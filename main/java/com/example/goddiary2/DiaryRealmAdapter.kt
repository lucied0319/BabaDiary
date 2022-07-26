package com.example.goddiary2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.goddiary2.databinding.CardLayoutBinding
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class DiaryRealmAdapter(data: OrderedRealmCollection<Diary>?,val listTag : List<String>, autoUpdate: Boolean) :
    RealmRecyclerViewAdapter<Diary, DiaryRealmAdapter.DiaryViewHolder>(data, autoUpdate) {

    inner class DiaryViewHolder(val binding : CardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardLayoutBinding.inflate(layoutInflater,parent,false)

        //ホルダー作成,日付を取得
        val holder = DiaryViewHolder(binding)

        ////短くサイクルビューをタップした時のリスナーをセット

        holder.itemView.setOnClickListener{
            val date = holder.binding.textCardDate.text.toString()
            val intent = Intent(it.context,ShowDiaryActivity::class.java)
            intent.putExtra("date",date)
            it.context.startActivity(intent)
        }

        ////長くリサイクルビューをタップした時のリスナーをセット

        holder.itemView.setOnLongClickListener{
            var date = holder.binding.textCardDate.text.toString()
            val dialog = AlertUpdateDeleteDialog.newInstance(date)
            dialog.show((it.context as ScrollingActivity).supportFragmentManager,"")
            //dialog.show(,"")
            return@setOnLongClickListener true
        }


        return holder
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        var diary = data?.get(position)
        diary?.run {
            holder.binding.textCardDate.text = date
            holder.binding.textCardTitle.text = title
            holder.binding.textCardBody.text = bodyText
            holder.binding.textCardTag.text = listTag[tag]
            val bitmap = MyUtils.getImageFromByte(byteImage)
            holder.binding.imageMain.setImageBitmap(bitmap)
        }

    }
}