package com.example.goddiary2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.goddiary2.databinding.FragmentMakeTagBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_make_tag.*

/////////////////////////////////////////////////////////////////////////////////////////////////

class MakeTagFragment : Fragment(),AlertSaveDialog.OnAlertListener {

    interface TagUpdateListener{
        fun TagUpdate()
    }

    lateinit var listener: TagUpdateListener

    var _binding : FragmentMakeTagBinding? = null
    val binding get() = _binding!!

    lateinit var realm : Realm
    lateinit var arrayEditText : Array<TextInputEditText>

    ////////////////////////////////////////////////////////////////////////////////////////

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TagUpdateListener){
            listener = context
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMakeTagBinding.inflate(inflater,container,false)

        binding.apply {

            ///////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////  各種Viewの設定 //////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////

            //１０個のエディットテキスト////////////////////////////////////////////////////////

            arrayEditText = arrayOf(editTag1,editTag2,editTag3,editTag4, editTag5,
                editTag6,editTag7,editTag8,editTag9,editTag10)

            //タグ文字列を取得してタグエディットに表示

            val i = 0
            val config = realm.where<Config>().equalTo("id",i).findFirst()

            if(config !=null){
                val arrayTag = config.tag.split(",")
                for (i in 0..9){
                    arrayEditText[i].setText(arrayTag[i+1])
                }
            }

            //タグ保存ボタン////////////////////////////////////////////////////////////////////

            // リスナーをセット
            buttonTagSave.setOnClickListener{
                val dialog = AlertSaveDialog()
                dialog.show(childFragmentManager,"タグ保存")
            }

            return root
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //タグ保存ボタン処理　※AlertSaveDialogのインターフェイスと関連付け

    override fun onSaveClick() {

        val stringBuilder = StringBuilder()
        stringBuilder.apply{
            append("タグなし")
            for (i in 0..9){
                append(",").append(arrayEditText[i].text.toString())
            }
        }
        val i = 0
        realm.executeTransaction{
            val config = realm.where<Config>().equalTo("id",i).findFirst()
            config?.tag = stringBuilder.toString()
        }
        listener.TagUpdate()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}