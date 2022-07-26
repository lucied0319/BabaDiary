package com.example.goddiary2

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.goddiary2.databinding.ActivityScrollingBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_scrolling.*

class ScrollingActivity : AppCompatActivity(), AlertUpdateDeleteDialog.OnAlertListener,
    MakeTagFragment.TagUpdateListener ,MakeDiaryFragment.DiaryImageClickListener,
    AlertChangeColorDialog.OnAlertChangeColorListener, AlertChangeTitleDialog.OnAlertChangeTitleListener{
    
    private lateinit var binding: ActivityScrollingBinding
    private lateinit var realm: Realm
    private lateinit var viewPager2Adapter: ViewPager2Adapter
    private  var flagImage : Int = -1 //0ならメイン画像、1なら日記画像

    private  val arrayColor : Array<Int> = arrayOf(Color.WHITE,Color.BLACK,Color.RED,
        Color.GREEN,Color.BLUE,Color.YELLOW)

    //////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //realmとviewBindingを初期化

        realm = Realm.getDefaultInstance()
        binding = ActivityScrollingBinding.inflate(layoutInflater)

        binding.apply {

            setContentView(root)
            setSupportActionBar(toolbar)

            //最初の起動でConfigデータがあるか確認、なければ作成

            var i: Int = 0
            realm.executeTransaction{
                var config = realm.where<Config>().equalTo("id", i).findFirst()
                if(config == null){
                    config = realm.createObject<Config>(i)
                }
                if(config.byteMainImage == null){
                    imageView.setImageResource(R.drawable.room3)
                }else{
                    imageView.setImageBitmap(MyUtils.getImageFromByte(config.byteMainImage))
                }
                toolbarLayout.title = config.diaryName
                toolbarLayout.setExpandedTitleColor(arrayColor[config.diaryTitleColor])
            }

            //viewPager2のページを作成

            var arrayFragment = ArrayList<Fragment>()
            arrayFragment.add(DiaryListFragment())
            arrayFragment.add(MakeDiaryFragment())
            arrayFragment.add(MakeTagFragment())

            viewPager2Adapter = ViewPager2Adapter(this@ScrollingActivity, arrayFragment)
            viewPager2.adapter = viewPager2Adapter

            //タブレイアウトとviewPager2を関連付け

            val array = arrayOf("日記一覧", "日記作成", "タグ作成")
            TabLayoutMediator(tabLayout, viewPager2,
                { tab, position -> tab.text = array[position] }).attach()

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // 画像の定数の設定

    companion object {
        //@JvmStatic
        val flagImageMain = 0 //メイン画像
        val flagImageDiary = 1 //日記画像
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //メニュー作成

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //メニュー選択

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            //メイン画像変更
            R.id.changeImage -> {
                requestWriteStorage(flagImageMain)
                true
            }
            //タイトル変更
            R.id.changeTitle ->{
                AlertChangeTitleDialog().show(supportFragmentManager,"タイトル")
                true
            }
            //タイトルカラー変更
            R.id.changeTitleColor ->{
                AlertChangeColorDialog().show(supportFragmentManager,"タイトルカラー")
                true
            }
            //タイトルカラー変更
            R.id.changeTitleInit ->{
                onTitleInit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //メニュータイトル変更時の処理　※ AlertTitleDialogのインタフェースと関連付け

    override fun onChangeTitle(title: String) {

        binding.toolbarLayout.title = title

        var i: Int = 0
        realm.executeTransaction {
            var config = realm.where<Config>().equalTo("id", i).findFirst()
            config?.diaryName = title
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //メニュータイトルカラー変更時の処理　※ AlertColorDialogのインタフェースと関連付け

    override fun onClick(p0: DialogInterface?, p1: Int) {

        binding.toolbarLayout.setExpandedTitleColor(arrayColor[p1])

        var i: Int = 0
        realm.executeTransaction{
            var config = realm.where<Config>().equalTo("id", i).findFirst()
            config?.diaryTitleColor = p1
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //メニュータイトル初期化時の処理　

    fun onTitleInit() {

        binding.apply {

            toolbarLayout.title = "ばぁば日記"
            toolbarLayout.setExpandedTitleColor(arrayColor[0])
            imageView.setImageResource(R.drawable.room3)

            var i: Int = 0
            realm.executeTransaction{
                var config = realm.where<Config>().equalTo("id", i).findFirst()
                config?.diaryName = "ばぁば日記"
                config?.diaryTitleColor = 0
                config?.byteMainImage = null
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //日記制作画面の日記画像タップ時の処理　※MakeDiaryFragmentのインタフェースと関連付け

    override fun onDiaryImageClick() {
        requestWriteStorage(flagImageDiary)
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //ストレージパーミッション許可確認

    fun requestWriteStorage(flagImage : Int){

        this.flagImage = flagImage
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ){
            requestPermissionLancher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }else{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setType("image/*")
            resultImageLancher.launch(intent)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //onRequestPermissionsResultが非推奨のためActivityResultLauncherインスタンスを作成

    val requestPermissionLancher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it){
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setType("image/*")
            resultImageLancher.launch(intent)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //onActivityResultが非推奨のためActivityResultLauncherインスタンスを作成

    val resultImageLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it?.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data

            //ScrollActivityのメイン画像
            if (flagImage == flagImageMain) {

                //メイン画像を表示
                binding.imageView.setImageURI(uri)

                //メイン画像を保存
                val bitmapDrawable = binding.imageView.drawable as BitmapDrawable
                realm.executeTransaction {
                    val i = 0
                    val config = realm.where<Config>().equalTo("id", i).findFirst()
                    config?.byteMainImage = MyUtils.getByteFromImage(bitmapDrawable.bitmap)
                }

            //MakeDiaryFragmentの日記画像
            }else if (flagImage == flagImageDiary){

                //日記画像を表示
                (viewPager2Adapter.listFragment[1] as MakeDiaryFragment).binding.imageDiaryPhoto.setImageURI(uri)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //日記削除の処理 ※AlertUpdateDeleteDialogのインタフェースと関連付け

    override fun onDeleteClick(date: String) {

        realm.executeTransaction{
            realm.where<Diary>().equalTo("date", date).findFirst()?.deleteFromRealm()
        }
        Snackbar.make(binding.root, "削除しました", Snackbar.LENGTH_SHORT).show()
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //日記再編集の処理　※AlertUpdateDeleteDialogのインタフェースと関連付け

    override fun onUpdateClick(date: String) {

        val listFragment = ArrayList<Fragment>()
        listFragment.add(DiaryListFragment())
        listFragment.add(MakeDiaryFragment.newInstance(date))
        listFragment.add(MakeTagFragment())

        viewPager2Adapter = ViewPager2Adapter(this, listFragment)
        binding.viewPager2.adapter = viewPager2Adapter
        binding.viewPager2.setCurrentItem(1)
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //タグ保存後の変更を各フラグメントに反映　※MakeTagFragmentのインタフェースと関連付け

    override fun TagUpdate() {

        val arrayFragment = ArrayList<Fragment>()
        arrayFragment.apply {
            add(DiaryListFragment())
            add(MakeDiaryFragment())
            add(MakeTagFragment())
        }
        viewPager2Adapter = ViewPager2Adapter(this, arrayFragment)
        binding.viewPager2.apply {
            adapter = viewPager2Adapter
            setCurrentItem(2)
        }
        Snackbar.make(binding.root, "保存しました", Snackbar.LENGTH_SHORT)
    }

    ////////////////////////////////////////////////////////////////////////////////////

    override fun onBackPressed() {
        //super.onBackPressed()
        var i = binding.viewPager2.currentItem -1
        if(i<0){i = 2}
        binding.viewPager2.setCurrentItem(i)
    }

    ////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


}