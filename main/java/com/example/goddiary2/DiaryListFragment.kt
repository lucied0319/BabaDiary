package com.example.goddiary2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goddiary2.databinding.FragmentDiaryListBinding
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

/////////////////////////////////////////////////////////////////////////////////////////////

class DiaryListFragment : Fragment() {

    private lateinit var realm: Realm

    private var _binding : FragmentDiaryListBinding? = null
    private  val binding get() = _binding!!


    var year : Int = 0
    val maxYear = 2040 //日記がつけれる最大の年号
    var month : Int = 0

    /////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()

        val calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)+ 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDiaryListBinding.inflate(inflater,container,false)

        binding.apply {

            ///////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////  各種Viewの設定 //////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////

            //年スピナー設定//////////////////////////////////////////////////////////////////////

            //maxYearを超える年なら年をmaxYearに設定

            if((maxYear - year) < 0) year = maxYear

            //アダプターの設定とセット

            var list = mutableListOf("2020")

            for (year in 2021..maxYear)
            list.add(year.toString())

            var adapter = ArrayAdapter<String>(requireContext(), R.layout.spinner4, list)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown)

            spinnerYear.adapter = adapter
            spinnerYear.setSelection(year - 2020)

            //月スピナー設定//////////////////////////////////////////////////////////////////////

            //アダプターの設定とセット

            list =  mutableListOf("1","2","3","4","5","6","7","8","9","10","11","12","全て")

            adapter = ArrayAdapter<String>(requireContext(), R.layout.spinner4, list)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown)

            spinnerMonth.adapter = adapter
            spinnerMonth.setSelection(month - 1)

            //タグスピナー設定/////////////////////////////////////////////////////////////////////

            //アダプター作成とセット

            var i = 0
            var config = realm.where<Config>().equalTo("id", i).findFirst()

            if (config != null) {

                list = mutableListOf()
                list.addAll(config.tag.split(","))
                var listTag = mutableListOf<String>()
                listTag.addAll(list)
                listTag.set(0,"全て")

                adapter = ArrayAdapter<String>(requireContext(), R.layout.spinner4, listTag)
                adapter.setDropDownViewResource(R.layout.spinner_dropdown)

                spinnerTag.adapter = adapter

            }

            //スピナー共通リスナーオブジェクト作成とセット

            val SpinnerListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    //それぞれのタグから現在の値を求める
                    var intYear = spinnerYear.selectedItem.toString().toInt()
                    var intMonth = spinnerMonth.selectedItemPosition + 1
                    var intTag = spinnerTag.selectedItemPosition

                    var realmResult: RealmResults<Diary>

                    when {
                        //月もタグも「全て」　※その年の日記全て

                        intTag == 0 && intMonth == 13 ->
                            realmResult = realm.where<Diary>().equalTo("year", intYear).findAll()
                                .sort("date", Sort.DESCENDING)

                        //月が「全て」　※その年のそのタグの日記全て

                        intMonth == 13 ->
                            realmResult =
                                realm.where<Diary>().equalTo("year", intYear).equalTo("tag", intTag)
                                    .findAll().sort("date", Sort.DESCENDING)

                        //タグが「全て」　※その年のその月の日記全て

                        intTag == 0 ->
                            realmResult = realm.where<Diary>().equalTo("year", intYear)
                                .equalTo("month", intMonth).findAll().sort("date", Sort.DESCENDING)

                        //月もタグも「全て」以外　※その年のその月のそのタグの日記全て

                        else ->
                            realmResult = realm.where<Diary>().equalTo("year", intYear)
                                .equalTo("month", intMonth).equalTo("tag", intTag).findAll()
                                .sort("date", Sort.DESCENDING)
                    }

                    val recycleAdapter = DiaryRealmAdapter(realmResult, list, true)
                    recyclerView.adapter = recycleAdapter
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

            spinnerYear.onItemSelectedListener = SpinnerListener
            spinnerMonth.onItemSelectedListener = SpinnerListener
            spinnerTag.onItemSelectedListener = SpinnerListener

            //リサイクルビュー///////////////////////////////////////////////////////////////////////

            //アダプター作成とセット

            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = linearLayoutManager

            val results = realm.where<Diary>().equalTo("year", year).equalTo("month", month)
                .findAll().sort("date", Sort.DESCENDING)

            val recycleAdapter = DiaryRealmAdapter(results, list, true)
            recyclerView.adapter = recycleAdapter

            return root
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DiaryListFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

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