package com.example.goddiary2

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.goddiary2.databinding.FragmentMakeDiaryBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

////////////////////////////////////////////////////////////////////////////////////////////

class MakeDiaryFragment : Fragment() ,DatePickerFragment.OnDatePickListener , AlertSaveDialog.OnAlertListener{

    interface DiaryImageClickListener{
        fun onDiaryImageClick()
    }
    private lateinit var listener : DiaryImageClickListener

    private  var _binding : FragmentMakeDiaryBinding? = null
    val binding get() = _binding!!
    private  lateinit var realm : Realm

    private var year : Int = 0
    private var month : Int = 0
    private var weather : Int = 0
    private var tag = 0
    private var date = ""

    ////////////////////////////////////////////////////////////////////////////////////////////

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DiaryImageClickListener){
            listener = context
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //リアムデータベース取得
        realm = Realm.getDefaultInstance()

        //今日の日付を取得するラムダ関数

        val makedate  ={
            val calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH ) +1
            String.format("%d/%02d/%02d",year, month,calendar.get(Calendar.DATE))
        }

        //Bundleにリストデータからの日付がある（日記再編集から作ったオブジェクト）なら取り出す、なければ今日の日付

        date = arguments?.getString("date") ?: makedate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMakeDiaryBinding.inflate(inflater,container,false)

        binding.apply {

            ///////////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////////////////////////////

            //日記画像/////////////////////////////////////////////////////////////////////////

            //日記画像タップ時のリスナーをセット

            imageDiaryPhoto.setOnClickListener{listener.onDiaryImageClick()}

            //日付ボタン////////////////////////////////////////////////////////////////////////

            buttonDate.text = date

            //リスナーをセット

            buttonDate.setOnClickListener{
                val dialog = DatePickerFragment()
                dialog.show(childFragmentManager,"日付")
            }

            //日記保存ボタン/////////////////////////////////////////////////////////////////////////

            // リスナーをセット

            buttonSave.setOnClickListener{
                val dialog = AlertSaveDialog()
                dialog.show(childFragmentManager,"保存")
            }

            //天気スピナー////////////////////////////////////////////////////////////////////////

            //リスナーオブジェクトの作成とセット

            val list =  mutableListOf("晴れ","曇り","雨","雷雨","雪","吹雪")
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.spinner4, list)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown)

            spinnerWeather.adapter = adapter
            spinnerWeather.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    weather = p2
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

            //タグスピナー///////////////////////////////////////////////////////////////////////

            //アダプター作成とセット

            var i = 0
            var config = realm.where<Config>().equalTo("id",i).findFirst()

            if(config !=null) {
                var arrayTag = config.tag.split(",")
                var adapter = ArrayAdapter<String>(requireContext(),R.layout.spinner4,arrayTag)
                adapter.setDropDownViewResource( R.layout.spinner_dropdown)
                spinnerTag.adapter = adapter
            }

            //リスナーオブジェクト作成とセット

            spinnerTag.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    tag = p2
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

            showDiary()

            return root
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //その日付の日記の内容を表示するメソッド

    fun showDiary(){

        var diary = realm.where<Diary>().equalTo("date",date).findFirst()

        binding.apply {

            //その日付の日記が存在する時

            if (diary != null){
                diary.let {
                    editTitle.setText(it.title)
                    editBodyText.setText(it.bodyText)
                    spinnerWeather.setSelection(it.weather)
                    spinnerTag.setSelection(it.tag)
                    year = it.year
                    month = it.month
                    weather = it.weather
                    tag = it.tag
                    if (it.byteImage != null) {
                        val bitmap = MyUtils.getImageFromByte(it.byteImage)
                        imageDiaryPhoto.setImageBitmap(bitmap)
                    }
                }

            //その日付の日記が存在しない時

            }else{
                editTitle.setText("")
                editBodyText.setText("")
                spinnerTag.setSelection(0)
                spinnerWeather.setSelection(0)
                imageDiaryPhoto.setImageResource(R.drawable.noimage)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        fun newInstance(date: String) =
            MakeDiaryFragment().apply {
                arguments = Bundle().apply {
                    putString("date", date)
                }
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //日付ボタン処理　※DatePickerFragmentのインターフェイスと関連付け

    override fun onDateSelect(year: Int, month: Int, day: Int) {

        date = "%d/%02d/%02d".format(year,month,day)
        this.year = year
        this.month = month
        binding.buttonDate.text = date
        showDiary()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //日記保存ボタン処理　※AlertSaveDialogのインターフェイスと関連付け

    override fun onSaveClick() {

        binding.apply {
            realm.executeTransaction{
                var diary = realm.where<Diary>().equalTo("date",date).findFirst()
                if(diary == null){
                    diary = realm.createObject(date)
                }
                diary.let {
                    it.title = editTitle.text.toString()
                    it.bodyText = editBodyText.text.toString()
                    it.year = year
                    it.month = month
                    it.weather = weather
                    it.tag = tag
                    val bitmapDrawable = imageDiaryPhoto.drawable as BitmapDrawable
                    val byteArray = MyUtils.getByteFromImage(bitmapDrawable.bitmap)
                    if (byteArray != null && byteArray.size > 0){
                        it.byteImage = byteArray
                    }
                }
            }
            Snackbar.make(root,"保存しました",Snackbar.LENGTH_SHORT).show()
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}