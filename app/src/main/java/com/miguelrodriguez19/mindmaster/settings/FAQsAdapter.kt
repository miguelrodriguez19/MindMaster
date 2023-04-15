package com.miguelrodriguez19.mindmaster.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miguelrodriguez19.mindmaster.R
import com.miguelrodriguez19.mindmaster.databinding.CellFaqBinding
import com.miguelrodriguez19.mindmaster.models.FAQ

class FAQsAdapter(
    var data: ArrayList<FAQ>, val onClick: (FAQ) -> Unit
) : RecyclerView.Adapter<FAQsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_faq, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val bind = CellFaqBinding.bind(v)
        private val cvFaqsArea = bind.cardviewFaqs
        private val llQuestion = bind.llQuestion
        private val llAnswer = bind.llAnswer
        private val tvQuestion = bind.tvFaqsQuestion
        private val tvAnswer = bind.tvFaqsAnswer
        private val btnSeeMore = bind.btnSeeMore

        fun bind(item: FAQ) {
            llQuestion.setOnClickListener {
                setVisibilityAnswer()
            }
            llAnswer.setOnClickListener {
                setVisibilityAnswer()
            }
            btnSeeMore.setOnClickListener {
                setVisibilityAnswer()
            }
            tvQuestion.text = item.question
            tvAnswer.text = item.answer
            cvFaqsArea.setOnClickListener {
                setVisibilityAnswer()
                onClick(item)
            }
        }

        private fun setVisibilityAnswer() {
            if (llAnswer.visibility == View.GONE) {
                llAnswer.visibility = View.VISIBLE
                btnSeeMore.rotation = 180F
            } else {
                llAnswer.visibility = View.GONE
                btnSeeMore.rotation = 0F
            }
        }
    }
}