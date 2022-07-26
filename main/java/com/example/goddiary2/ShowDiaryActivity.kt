package com.example.goddiary2

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.goddiary2.databinding.ActivityShowDiaryBinding
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.content_scrolling.*

//////////////////////////////////////////////////////////////////////////////////////////////

class ShowDiaryActivity : AppCompatActivity(), AlertChangeColorDialog.OnAlertChangeColorListener{

    private lateinit var binding : ActivityShowDiaryBinding
    private lateinit var realm : Realm

    private var date : String? = null
    private val arrayColor : Array<Int> = arrayOf(Color.WHITE,Color.BLACK,Color.RED,
        Color.GREEN,Color.BLUE,Color.YELLOW)

    //////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        binding = ActivityShowDiaryBinding.inflate(layoutInflater)

        binding.apply {

            setContentView(root)
            setSupportActionBar(toolbar)

            //日記表示

            val i = 0
            val arrayTag = realm.where<Config>().equalTo("id",i).findFirst()?.tag?.split(",")

            val arrayWheather = arrayOf("晴れ","曇り","雨","雷雨","雪","吹雪")

            date = intent.getStringExtra("date")
            val diary = realm.where<Diary>().equalTo("date",date).findFirst()

            diary?.run {
                textTitle.text = date
                textWeather.text = arrayWheather[weather]
                textTag.text = arrayTag?.get(tag)
                textBody.text = bodyText
                toolbarLayout.title = title
                toolbarLayout.setExpandedTitleColor(arrayColor[dateColor])
                val bitmap = MyUtils.getImageFromByte(byteImage)
                imageView.setImageBitmap(bitmap)
            }

            //戻るボタン////////////////////////////////////////////////////////////////////
            buttonBack.setOnClickListener { finish() }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //メニュー作成

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_show_diary, menu)
        return true
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //メニュー選択

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            //日付カラー変更
            R.id.changeDateColor -> {
                AlertChangeColorDialog().show(supportFragmentManager,"日付カラー変更")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //メニュー日付カラー変更処理　※ AlertColorDialogのインタフェースと関連付け

    override fun onClick(p0: DialogInterface?, p1: Int) {

        binding.toolbarLayout.setExpandedTitleColor(arrayColor[p1])

        var i: Int = 0
        realm.executeTransaction{
            var diary = realm.where<Diary>().equalTo("date", date).findFirst()
            diary?.dateColor = p1
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}