package io.signy.signysdk.activity.addDocument.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.signy.signysdk.R
import io.signy.signysdk.model.Document
import io.signy.signysdk.others.interfaces.ListListener
import kotlinx.android.synthetic.main.signy_sdk_item_document_row.view.*


class DocumentAdapter(
    private val documentList: MutableList<Document>,
    private val listListener: ListListener
) :
    RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.signy_sdk_item_document_row,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val document = documentList[position];
        holder.tvDocumentName.text = document.name
        holder.itemView.setOnClickListener {
            listListener.onClickOnItem(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDocumentName: TextView = view.tvDocumentName
    }
}