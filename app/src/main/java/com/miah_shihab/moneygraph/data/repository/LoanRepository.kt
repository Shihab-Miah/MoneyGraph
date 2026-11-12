package com.miah_shihab.moneygraph.data.repository

import com.miah_shihab.moneygraph.data.db.LoanDao
import com.miah_shihab.moneygraph.data.db.LoanEntity
import com.miah_shihab.moneygraph.data.db.LoanPaymentDao
import com.miah_shihab.moneygraph.data.db.LoanPaymentEntity
import com.miah_shihab.moneygraph.domain.model.Loan
import com.miah_shihab.moneygraph.domain.model.LoanPayment
import com.miah_shihab.moneygraph.domain.model.LoanStatus
import com.miah_shihab.moneygraph.domain.model.LoanType
import com.miah_shihab.moneygraph.util.generateId
import com.miah_shihab.moneygraph.util.toBigDecimalSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val loanDao: LoanDao,
    private val loanPaymentDao: LoanPaymentDao
) {

    fun getAll(): Flow<List<Loan>> {
        return combine(
            loanDao.getAllFlow(),
            loanPaymentDao.getAllFlow()
        ) { loans, payments ->
            val paymentMap = payments.groupBy { it.payment_loan_id }
            loans.map { entity ->
                val pList = paymentMap[entity.loan_id]?.map { it.toDomain() } ?: emptyList()
                entity.toDomain(pList)
            }
        }
    }

    fun getActive(): Flow<List<Loan>> {
        return combine(
            loanDao.getActiveFlow(),
            loanPaymentDao.getAllFlow()
        ) { loans, payments ->
            val paymentMap = payments.groupBy { it.payment_loan_id }
            loans.map { entity ->
                val pList = paymentMap[entity.loan_id]?.map { it.toDomain() } ?: emptyList()
                entity.toDomain(pList)
            }
        }
    }

    suspend fun getById(id: Long): Loan? {
        val entity = loanDao.getById(id) ?: return null
        val payments = loanPaymentDao.getByLoanId(id).map { it.toDomain() }
        return entity.toDomain(payments)
    }

    suspend fun insertLoan(
        personName: String,
        amount: BigDecimal,
        type: LoanType,
        dueDate: Long = 0L,
        note: String = ""
    ): Long {
        val id = generateId()
        val entity = LoanEntity(
            loan_id = id,
            loan_person_name = personName,
            loan_amount = amount.toPlainString(),
            loan_paid_amount = "0.00",
            loan_due_date = dueDate,
            loan_note = note,
            loan_type = type.value,
            loan_status = LoanStatus.ACTIVE.value,
            loan_created_time = System.currentTimeMillis()
        )
        loanDao.insert(entity)
        return id
    }

    suspend fun addPayment(
        loanId: Long,
        paymentAmount: BigDecimal,
        note: String = "",
        time: Long = System.currentTimeMillis()
    ): Long {
        val loan = loanDao.getById(loanId) ?: return -1
        val paymentId = generateId()
        val paymentEntity = LoanPaymentEntity(
            payment_id = paymentId,
            payment_loan_id = loanId,
            payment_amount = paymentAmount.toPlainString(),
            payment_time = time,
            payment_note = note
        )
        loanPaymentDao.insert(paymentEntity)

        // Recalculate paid amount and status
        val currentPaid = loan.loan_paid_amount.toBigDecimalSafe()
        val newPaid = currentPaid.add(paymentAmount)
        val originalAmount = loan.loan_amount.toBigDecimalSafe()

        val newStatus = when {
            newPaid >= originalAmount -> LoanStatus.SETTLED.value
            newPaid > BigDecimal.ZERO -> LoanStatus.PARTIAL.value
            else -> LoanStatus.ACTIVE.value
        }

        loanDao.update(
            loan.copy(
                loan_paid_amount = newPaid.toPlainString(),
                loan_status = newStatus
            )
        )
        return paymentId
    }

    suspend fun update(loan: Loan) {
        val entity = LoanEntity(
            loan_id = loan.id,
            loan_person_name = loan.personName,
            loan_amount = loan.amount.toPlainString(),
            loan_paid_amount = loan.paidAmount.toPlainString(),
            loan_due_date = loan.dueDate,
            loan_note = loan.note,
            loan_type = loan.type.value,
            loan_status = loan.status.value,
            loan_created_time = loan.createdTime
        )
        loanDao.update(entity)
    }

    suspend fun delete(id: Long) {
        loanDao.deleteById(id)
    }

    private fun LoanEntity.toDomain(payments: List<LoanPayment>): Loan {
        val total = loan_amount.toBigDecimalSafe()
        val paid = loan_paid_amount.toBigDecimalSafe()
        val remaining = total.subtract(paid).max(BigDecimal.ZERO)

        val type = if (loan_type == 1) LoanType.LENT else LoanType.BORROWED
        val status = when (loan_status) {
            1 -> LoanStatus.ACTIVE
            2 -> LoanStatus.PARTIAL
            3 -> LoanStatus.SETTLED
            else -> LoanStatus.ACTIVE
        }

        return Loan(
            id = loan_id,
            personName = loan_person_name,
            amount = total,
            paidAmount = paid,
            remainingAmount = remaining,
            dueDate = loan_due_date,
            note = loan_note,
            type = type,
            status = status,
            createdTime = loan_created_time,
            payments = payments
        )
    }

    private fun LoanPaymentEntity.toDomain(): LoanPayment = LoanPayment(
        id = payment_id,
        loanId = payment_loan_id,
        amount = payment_amount.toBigDecimalSafe(),
        time = payment_time,
        note = payment_note
    )
}
