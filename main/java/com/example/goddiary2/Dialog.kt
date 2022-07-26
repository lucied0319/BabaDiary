package com.example.goddiary2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.goddiary2.databinding.EditDialogBinding
import java.util.*

////////////////////////////////////////////////////////////////////////////////////
//日付ダイアログ

class DatePickerFragment : DialogFragment(), OnDateSetListener{

    interface OnDatePickListener{
        fun onDateSelect(year: Int,month: Int,day:Int)
    }
    private lateinit var listener: OnDatePickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(parentFragment is OnDatePickListener){
            listener = parentFragment as OnDatePickListener
        }
    }
    //DatePickerDialogインスタンスの作成
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(),this,year,month,day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        listener.onDateSelect(p1,p2+1,p3)
    }

}

///////////////////////////////////////////////////////////////////////////////////////
//保存確認ダイアログ

class AlertSaveDialog : DialogFragment(){

    interface OnAlertListener{
        fun onSaveClick()
    }

    private  lateinit var listener : OnAlertListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            parentFragment is OnAlertListener->
                listener = parentFragment as OnAlertListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity).apply {
            setTitle("保存確認")
            setMessage("この内容で保存してもいいですか？")
            setPositiveButton("OK"){dialog,which ->
                listener.onSaveClick()
            }
            setNegativeButton("キャンセル"){dialog,which ->
            }
        }
        return builder.create()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//再編集、削除確認ダイアログ

class AlertUpdateDeleteDialog : DialogFragment(){

    interface OnAlertListener{
        fun onDeleteClick(date: String)
        fun onUpdateClick(date: String)
    }
    private lateinit var listener: OnAlertListener

    private var date : String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            context is OnAlertListener ->{
                listener = context
            }
        }
    }
    companion object {
        @JvmStatic
        fun newInstance(date: String) =
            AlertUpdateDeleteDialog().apply {
                arguments = Bundle().apply {
                    putString("date",date)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getString("date") ?: ""
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arrayOf("再編集","削除","キャンセル")
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setTitle("メニュー")
            setItems(items) {dialoginterface,i ->
                when{
                    //再編集をタップ
                    i == 0 -> listener.onUpdateClick(date)
                    //削除をタップ
                    i == 1 ->{
//                        val dialog = AlertDeleteDialog()
//                        dialog.show(parentFragmentManager,"")
                        val builder = AlertDialog.Builder(activity).apply {
                            setTitle("削除確認")
                            setMessage("この日記を削除してもいいですか？")
                            setPositiveButton("OK"){dialog,which ->
                                listener.onDeleteClick(date)
                            }
                            setNegativeButton("キャンセル"){dialog,which ->
                            }
                        }
                        builder.show()
                    }
                }
            }
        }
        return builder.create()
    }
}

//////////////////////////////////////////////////////////////////////////////////////
//ダイアログタイトル変更ダイアログ

class AlertChangeTitleDialog : DialogFragment(){

    interface OnAlertChangeTitleListener {
        fun onChangeTitle(title : String)
    }

    private  lateinit var listener : OnAlertChangeTitleListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            context is OnAlertChangeTitleListener->
                listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val layoutInflater = LayoutInflater.from(activity)
        val binding = EditDialogBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(activity).apply {
            setTitle("日記名変更")

            setPositiveButton("OK") { dialog,which ->
                listener.onChangeTitle(binding.editTextDialog.text.toString())
            }

            setView(binding.root)
            setNegativeButton("キャンセル"){dialog,which ->
            }
        }
        return builder.create()
    }
}

////////////////////////////////////////////////////////////////////////////////////////
//ダイアログタイトルカラー変更ダイアログ

class AlertChangeColorDialog : DialogFragment(){

    interface OnAlertChangeColorListener : DialogInterface.OnClickListener

    private  lateinit var listener : OnAlertChangeColorListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            context is OnAlertChangeColorListener->
                listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val arrayColors : Array<String> = arrayOf("白","黒","赤","緑","青","黄")

        val builder = AlertDialog.Builder(activity).apply {
            setTitle("文字色変更")
            setItems(arrayColors,listener)
            setNegativeButton("キャンセル"){dialog,which ->
            }
        }
        return builder.create()
    }
}

