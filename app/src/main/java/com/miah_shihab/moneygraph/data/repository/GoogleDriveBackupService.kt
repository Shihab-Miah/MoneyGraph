package com.miah_shihab.moneygraph.data.repository

import android.content.Context
import android.util.Base64
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.miah_shihab.moneygraph.data.db.AccountDao
import com.miah_shihab.moneygraph.data.db.BudgetDao
import com.miah_shihab.moneygraph.data.db.CategoryDao
import com.miah_shihab.moneygraph.data.db.LoanDao
import com.miah_shihab.moneygraph.data.db.LoanPaymentDao
import com.miah_shihab.moneygraph.data.db.RecordDao
import com.miah_shihab.moneygraph.util.BACKUP_AES_KEY
import com.miah_shihab.moneygraph.util.BACKUP_FILE_EXTENSION
import com.miah_shihab.moneygraph.util.BACKUP_SALT
import com.miah_shihab.moneygraph.util.DRIVE_FOLDER_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class BackupPayload(
    val version: Int = 1,
    val app: String = "MoneyGraph",
    val exportedBy: String = "miah_shihab",
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<com.miah_shihab.moneygraph.data.db.AccountEntity>,
    val categories: List<com.miah_shihab.moneygraph.data.db.CategoryEntity>,
    val records: List<com.miah_shihab.moneygraph.data.db.RecordEntity>,
    val budgets: List<com.miah_shihab.moneygraph.data.db.BudgetEntity>,
    val loans: List<com.miah_shihab.moneygraph.data.db.LoanEntity>,
    val loanPayments: List<com.miah_shihab.moneygraph.data.db.LoanPaymentEntity>
)

data class DriveBackupFile(
    val id: String,
    val name: String,
    val size: Long,
    val modifiedTime: Long
)

@Singleton
class GoogleDriveBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val recordDao: RecordDao,
    private val budgetDao: BudgetDao,
    private val loanDao: LoanDao,
    private val loanPaymentDao: LoanPaymentDao,
    private val preferencesRepository: PreferencesRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("MoneyGraph").build()
    }

    suspend fun createEncryptedBackupString(): String = withContext(Dispatchers.IO) {
        val payload = BackupPayload(
            accounts = accountDao.getAll(),
            categories = categoryDao.getAll(),
            records = recordDao.getAll(),
            budgets = budgetDao.getAll(),
            loans = loanDao.getAll(),
            loanPayments = loanPaymentDao.getAll()
        )
        val json = gson.toJson(payload)
        encryptPayload(json)
    }

    suspend fun restoreFromEncryptedString(encryptedData: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = decryptPayload(encryptedData)
            val payload = gson.fromJson(json, BackupPayload::class.java)

            // Restore data into database
            for (acc in payload.accounts) accountDao.insert(acc)
            for (cat in payload.categories) categoryDao.insert(cat)
            for (rec in payload.records) recordDao.insert(rec)
            for (bug in payload.budgets) budgetDao.insert(bug)
            for (loan in payload.loans) loanDao.insert(loan)
            for (p in payload.loanPayments) loanPaymentDao.insert(p)

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun uploadBackupToDrive(account: GoogleSignInAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            val folderId = getOrCreateAppFolder(drive)

            val encryptedContent = createEncryptedBackupString()
            val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "MoneyGraph_Backup_$timeStr$BACKUP_FILE_EXTENSION"

            val tempFile = java.io.File(context.cacheDir, filename)
            tempFile.writeText(encryptedContent)

            val fileMetadata = File().apply {
                name = filename
                parents = listOf(folderId)
                description = "MoneyGraph automated encrypted backup"
            }

            val mediaContent = FileContent("application/octet-stream", tempFile)
            val uploaded = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name, createdTime")
                .execute()

            tempFile.delete()
            preferencesRepository.setLastBackupTime(System.currentTimeMillis())
            Result.success(uploaded.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listBackupsFromDrive(account: GoogleSignInAccount): Result<List<DriveBackupFile>> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            val folderId = getOrCreateAppFolder(drive)

            val query = "'$folderId' in parents and name contains '$BACKUP_FILE_EXTENSION' and trashed = false"
            val fileList = drive.files().list()
                .setQ(query)
                .setFields("files(id, name, size, modifiedTime)")
                .setOrderBy("modifiedTime desc")
                .execute()

            val backups = fileList.files?.map { f ->
                DriveBackupFile(
                    id = f.id,
                    name = f.name,
                    size = f.getSize() ?: 0L,
                    modifiedTime = f.modifiedTime?.value ?: 0L
                )
            } ?: emptyList()

            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromDriveFile(account: GoogleSignInAccount, fileId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val encryptedString = outputStream.toString("UTF-8")

            val success = restoreFromEncryptedString(encryptedString)
            if (success) Result.success(true) else Result.failure(Exception("Failed to decrypt or parse backup file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getOrCreateAppFolder(drive: Drive): String {
        val query = "mimeType = 'application/vnd.google-apps.folder' and name = '$DRIVE_FOLDER_NAME' and trashed = false"
        val result = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        if (!files.isNullOrEmpty()) {
            return files[0].id
        }

        val folderMetadata = File().apply {
            name = DRIVE_FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        val folder = drive.files().create(folderMetadata)
            .setFields("id")
            .execute()

        return folder.id
    }

    // ─────────────────────────────────────────────
    // AES-256-CBC Encryption matching MyMoney Pro
    // Format: Base64(IV):Base64(Ciphertext):Base64(HMAC)
    // ─────────────────────────────────────────────
    private fun encryptPayload(plainText: String): String {
        val salt = BACKUP_SALT.toByteArray(Charsets.UTF_8)
        val keySpec = PBEKeySpec(BACKUP_AES_KEY.toCharArray(), salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // HMAC-SHA256
        val mac = Mac.getInstance("HmacSHA256")
        val macKey = SecretKeySpec(secretKey.encoded, "HmacSHA256")
        mac.init(macKey)
        val hmac = mac.doFinal(cipherText)

        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        val hmacBase64 = Base64.encodeToString(hmac, Base64.NO_WRAP)

        return "$ivBase64:$cipherBase64:$hmacBase64"
    }

    private fun decryptPayload(encrypted: String): String {
        val parts = encrypted.split(":")
        if (parts.size != 3) throw IllegalArgumentException("Invalid backup format")

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
        val expectedHmac = Base64.decode(parts[2], Base64.NO_WRAP)

        val salt = BACKUP_SALT.toByteArray(Charsets.UTF_8)
        val keySpec = PBEKeySpec(BACKUP_AES_KEY.toCharArray(), salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, "AES")

        // Verify HMAC
        val mac = Mac.getInstance("HmacSHA256")
        val macKey = SecretKeySpec(secretKey.encoded, "HmacSHA256")
        mac.init(macKey)
        val calculatedHmac = mac.doFinal(cipherText)
        if (!calculatedHmac.contentEquals(expectedHmac)) {
            throw SecurityException("Backup integrity check failed: HMAC mismatch")
        }

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, Charsets.UTF_8)
    }
}
