package com.jabirdeveloper.tinderswipe

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.jabirdeveloper.tinderswipe.Chat.ChatActivity
import com.jabirdeveloper.tinderswipe.Listcard.ListcardActivity
import com.jabirdeveloper.tinderswipe.Matches.MatchesActivity
import com.jabirdeveloper.tinderswipe.Switch_pageActivity
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import java.io.IOException
import java.util.*

class Switch_pageActivity : AppCompatActivity() {
    private lateinit var selectedFragment: Fragment
    private lateinit var bottomNav: BottomNavigationView
    private var id = R.id.item2
    private var language: String? = null
    private val Accept: String? = null
    private var First: String = ""
    private lateinit var dialog: Dialog
    lateinit var mInterstitialAd: InterstitialAd
    lateinit var rewardedAd: RewardedAd
    private val page1 = SettingMainActivity()
    private val page2 = MainActivity()
    private val page3 = ListcardActivity()
    private val page4 = MatchesActivity()
    private  var functions = Firebase.functions
    private var activeFragment: Fragment = MainActivity()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocal()
        setContentView(R.layout.activity_switch_page)

        getMyUser()
       /* MobileAds.initialize(this) {}
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.show()*/
        rewardedAd = RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                Toast.makeText(this@Switch_pageActivity, "สวย", Toast.LENGTH_SHORT).show()
            }
            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                Toast.makeText(this@Switch_pageActivity, errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        bar = findViewById(R.id.bar2)
        if (intent.hasExtra("NewMatch")) {
            val uid = intent.getStringArrayListExtra("NewMatch")
            getDatabase(uid[0])
            //final ArrayList<String> name = getIntent().getStringArrayListExtra("NameMatch");
            //Toast.makeText(Switch_pageActivity.this,name.get(0),Toast.LENGTH_SHORT).show();
            /*for(int i = 0 ; i<2;i++) {
                final Dialog dialog2 = new Dialog(Switch_pageActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.show_match, null);
                ImageView imageView = view.findViewById(R.id.image_match);
                ImageView star = view.findViewById(R.id.star);
                TextView textView = view.findViewById(R.id.textmatch);
                TextView textView2 = view.findViewById(R.id.io);
                TextView textView3 = view.findViewById(R.id.textBig);
                TextView textView4 = view.findViewById(R.id.textmatch2);
                Button button = view.findViewById(R.id.mess);
                textView.setText("กายน่ารัก");
                textView4.setText("  " + "ถูกใจคุณเหมือนกัน");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog2.dismiss();
                        Intent intent = new Intent(Switch_pageActivity.this, ChatActivity.class);
                        Bundle b = new Bundle();
                        b.putString("time_chk", "");
                        b.putString("matchId", uid.get(0));
                        b.putString("nameMatch", "กายน่ารัก");
                        b.putString("first_chat", "");
                        b.putString("unread", "0");
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });
                textView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog2.dismiss();
                    }
                });
                Glide.with(this).load(R.drawable.ic_profile).into(imageView);
                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
                dialog2.setContentView(view);
                dialog2.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialog2.show();
            }*/
        }
        if (intent.hasExtra("warning")) {
            val choice = this.resources.getStringArray(R.array.report_item)
            var nameAndValue = ""
            val name = intent.getStringArrayListExtra("warning")
            val value = intent.getIntegerArrayListExtra("warning_value")
            for (i in name.indices) {
                nameAndValue += "${i + 1}.${choice[Integer.valueOf(name[i])]}${getString(R.string.count_report)}	${value[i]} ${getString(R.string.times)}"
            }

            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.warning_dialog, null)
            dialog = Dialog(this@Switch_pageActivity)
            val textView = view.findViewById<TextView>(R.id.text_warning)
            textView.text = nameAndValue
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(view)
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            dialog.window!!.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialog.show()
        }
        if (intent.hasExtra("first")) {
            First = intent.getStringExtra("first")
            if (First != "0") {
                bar!!.showBadge(R.id.item4, Integer.valueOf(First))
            }
            id = R.id.item2
            intent.removeExtra("first")
        }
        if (intent.hasExtra("accept")) {
            id = R.id.item4
            intent.removeExtra("accept")
        }
        if (intent.hasExtra("back")) {
            id = R.id.item1
            intent.removeExtra("back")
        }

        bar!!.setOnItemSelectedListener(object : ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(i: Int) {
                Log.d("num", i.toString())
                when (i) {
                    R.id.item1 -> {
                        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).hide(activeFragment).show(page1).commit()
                        activeFragment = page1
                        true
                    }
                    R.id.item2 -> {
                        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).hide(activeFragment).show(page2).commit()
                        activeFragment = page2
                        true
                    }
                    R.id.item3 -> {
                        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).hide(activeFragment).show(page3).commit()
                        activeFragment = page3
                        true
                    }
                    R.id.item4 -> {
                        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).hide(activeFragment).show(page4).commit()
                        activeFragment = page4
                        true
                    }
                    else -> false
                }
            }
        })
    }
    fun createAndLoadRewardedAd(): RewardedAd {
        val rewardedAd = RewardedAd(this, "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
            }
            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }
    private fun getMyUser()
    {
        Log.d("tagh","1")
        val MyUser = getSharedPreferences("MyUser", Context.MODE_PRIVATE).edit()
        val userDb = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().uid.toString())
        userDb.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

             /*   val gender  = if (dataSnapshot.child("sex").value == "Male") {
                    MyUser.putInt("gender",R.drawable.ic_man)
                } else MyUser.putInt("gender",R.drawable.ic_woman)*/

                if (dataSnapshot.hasChild("Vip")) {
                    MyUser.putBoolean("Vip", true)
                }
                else MyUser.putBoolean("Vip", false)
                if (dataSnapshot.child("connection").hasChild("yep")) {
                    MyUser.putInt("c",dataSnapshot.child("connection").child("yep").childrenCount.toInt())
                }
                if (dataSnapshot.hasChild("see_profile")) {
                    MyUser.putInt("s",dataSnapshot.child("see_profile").childrenCount.toInt())
                }
                    MyUser.putString("name",dataSnapshot.child("name").value.toString())
                    MyUser.putInt("Age",dataSnapshot.child("Age").value.toString().toInt())
                    MyUser.putInt("MaxLike",dataSnapshot.child("MaxLike").value.toString().toInt())
                    MyUser.putInt("MaxChat",dataSnapshot.child("MaxChat").value.toString().toInt())
                    MyUser.putInt("MaxAdmob",dataSnapshot.child("MaxAdmob").value.toString().toInt())
                    MyUser.putInt("MaxStar",dataSnapshot.child("MaxStar").value.toString().toInt())
                    MyUser.putInt("OppositeUserAgeMin",dataSnapshot.child("OppositeUserAgeMin").value.toString().toInt())
                    MyUser.putInt("OppositeUserAgeMax",dataSnapshot.child("OppositeUserAgeMax").value.toString().toInt())
                    MyUser.putString("OppositeUserSex",dataSnapshot.child("OppositeUserSex").value.toString())
                    MyUser.putString("Distance",dataSnapshot.child("Distance").value.toString())

                if (dataSnapshot.hasChild("Location")) {
                    MyUser.putString("X",dataSnapshot.child("Location").child("X").value.toString())
                    MyUser.putString("Y",dataSnapshot.child("Location").child("Y").value.toString())

                }
                if (dataSnapshot.child("ProfileImage").hasChild("profileImageUrl0")) {
                    MyUser.putString("image",dataSnapshot.child("ProfileImage").child("profileImageUrl0").value.toString())

                }
                else
                {
                    MyUser.putString("image","")
                }
                MyUser.apply()
                supportFragmentManager.beginTransaction().apply { add(R.id.fragment_container2,page1).hide(page1)
                    add(R.id.fragment_container2,page2).hide(page2)
                    add(R.id.fragment_container2,page3).hide(page3)
                    add(R.id.fragment_container2,page4).hide(page4)}.commit()
                    bar!!.setItemSelected(id, true)


            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("tagh","3")
            }

        })
    }

    private fun getDatabase(uid: String) {
        val userDb = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        userDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var url0 = "null"
                url0 = snapshot.child("ProfileImage").child("profileImageUrl0").value.toString()
                val name = snapshot.child("name").value.toString()
                val dialog2 = Dialog(this@Switch_pageActivity)
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.show_match, null)
                val imageView = view.findViewById<ImageView>(R.id.image_match)
                val star = view.findViewById<ImageView>(R.id.star)
                val textView = view.findViewById<TextView>(R.id.textmatch)
                val textView2 = view.findViewById<TextView>(R.id.io)
                val textView3 = view.findViewById<TextView>(R.id.textBig)
                val textView4 = view.findViewById<TextView>(R.id.textmatch2)
                val button = view.findViewById<Button>(R.id.mess)
                textView.text = name
                textView4.text = "  " + "ถูกใจคุณเหมือนกัน"
                button.setOnClickListener {
                    dialog2.dismiss()
                    val intent = Intent(this@Switch_pageActivity, ChatActivity::class.java)
                    val b = Bundle()
                    b.putString("time_chk", "")
                    b.putString("matchId", uid)
                    b.putString("nameMatch", "กายน่ารัก")
                    b.putString("first_chat", "")
                    b.putString("unread", "0")
                    intent.putExtras(b)
                    startActivity(intent)
                }
                textView2.setOnClickListener { dialog2.dismiss() }
                Glide.with(this@Switch_pageActivity).load(url0).into(imageView)
                dialog2.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog2.setContentView(view)
                dialog2.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                dialog2.show()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setCurrentIndex(newValueFormCurrentIndex: Int) {
        if (newValueFormCurrentIndex > 0) {
            bar!!.showBadge(R.id.item4, newValueFormCurrentIndex)
        } else {
            bar!!.dismissBadge(R.id.item4)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 8) {
            Toast.makeText(this@Switch_pageActivity, "fail GPS", Toast.LENGTH_SHORT).show()
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this@Switch_pageActivity, "fail GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val read_input = 0
    override fun onResume() {
        /*final SharedPreferences mySharedPreferences = this.getSharedPreferences("SentRead", Context.MODE_PRIVATE);
        int read = mySharedPreferences.getInt("Read",0 );

        final SharedPreferences SharedRead = this.getSharedPreferences("SentRead", Context.MODE_PRIVATE);
        int left_total = SharedRead.getInt("ReadFirst",0 );
        read_input = read_input+read;
        mySharedPreferences.edit().clear().commit();
        //Toast.makeText(Switch_pageActivity.this,(left)+" , "+(read),Toast.LENGTH_SHORT).show();
        if(read != 0){
            int Restore =  left-read;
            left = Restore;
            if(Restore > 0) {
                bar.showBadge(R.id.item4, Restore);
            }else{
                bar.dismissBadge(R.id.item4);
                left = 0;
            }
        }*/
        super.onResume()
    }

    var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
    }

    fun setLocal(lang: String?) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val configuration = Configuration()
        resources.configuration.setLocale(locale)
        baseContext.resources.updateConfiguration(configuration, baseContext.resources.displayMetrics)
        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", lang)
        editor.apply()
        Log.d("My", lang)
    }

    fun loadLocal() {
        val preferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val langure = preferences.getString("My_Lang", "")
        language = langure
        Log.d("My2", langure)
        setLocal(langure)
    }

    companion object {
        var bar: ChipNavigationBar? = null
        fun hide() {
            bar!!.visibility = View.GONE
        }
    }
}