package com.jabirdeveloper.tinderswipe

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.*
import android.widget.*
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import com.github.demono.AutoScrollViewPager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.jabirdeveloper.tinderswipe.Cards.ArrayAdapter
import com.jabirdeveloper.tinderswipe.Cards.Cards
import com.jabirdeveloper.tinderswipe.Chat.ChatActivity
import com.jabirdeveloper.tinderswipe.Functions.*
import com.jabirdeveloper.tinderswipe.QAStore.DialogFragment
import com.jabirdeveloper.tinderswipe.ViewModel.QuestionViewModel
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.activity_like_you.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import me.relex.circleindicator.CircleIndicator
import java.io.IOException
import java.lang.Runnable
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.coroutineScope as coroutineScope

@Suppress("NAME_SHADOWING")
class MainActivity : Fragment(), BillingProcessor.IBillingHandler, View.OnClickListener {

    private lateinit var mLocationManager: LocationManager
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGPSDialog: Dialog
    private lateinit var oppositeUserSex: String
    private var dis: String? = null
    private var oppositeUserAgeMin = 0
    private var oppositeUserAgeMax = 0
    private lateinit var arrayAdapter: ArrayAdapter
    private lateinit var usersDb: DatabaseReference
    private var distance = 0.0
    private var xUser = 0.0
    private var yUser = 0.0
    private lateinit var like: Button
    private lateinit var dislike: Button
    private lateinit var star: Button
    private lateinit var layoutGps: ConstraintLayout
    private lateinit var anime1: ImageView
    private lateinit var anime2: ImageView
    private lateinit var textgps: TextView
    private lateinit var textGps2: TextView
    private lateinit var handler: Handler
    private var maxLike = 0
    private var maxStar = 0
    private var maxAdmob = 0
    private lateinit var dialog: Dialog
    private var statusVip = false
    private lateinit var rowItem: ArrayList<Cards>
    private lateinit var po: Cards
    private lateinit var currentUid: String
    private var timeSend: String? = null
    private lateinit var touchGps: ImageView
    private var notificationMatch: String? = null
    private lateinit var cardStackView: CardStackView
    lateinit var manager: CardStackLayoutManager
    private var functions = Firebase.functions
    lateinit var rewardedAd: RewardedAd
    private lateinit var bp: BillingProcessor
    private var countLimit = 0
    private var countLimit2 = 0
    private var countLimit3 = 1
    private var countDataSet = 60
    private lateinit var resultlimit: ArrayList<*>
    private var checkEmpty = false
    private var empty = 0
    private var countEmpty = 0
    private lateinit var localizationDelegate: LocalizationActivityDelegate
    private lateinit var questionViewModel: QuestionViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("ghj", "สร้างละ")
        val view = inflater.inflate(R.layout.activity_main, container, false)
        checkStart()
        bp = BillingProcessor(requireContext(), Id.Id, this)
        bp.initialize()
        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser == null) {
            val intent = Intent(context, ChooseLoginRegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        localizationDelegate = LocalizationActivityDelegate(requireActivity())
        layoutGps = view.findViewById(R.id.layout_in)
        textgps = view.findViewById(R.id.textView8)
        textGps2 = view.findViewById(R.id.textView9)
        touchGps = view.findViewById(R.id.imageView3)
        textgps.setText(R.string.touch_settings)
        textGps2.setText(R.string.Area)
        like = view.findViewById(R.id.like_button)
        dislike = view.findViewById(R.id.dislike_button)
        star = view.findViewById(R.id.star_button)
        anime1 = view.findViewById(R.id.anime1)
        anime2 = view.findViewById(R.id.anime2)
        rowItem = ArrayList()
        currentUid = mAuth.currentUser!!.uid
        usersDb = FirebaseDatabase.getInstance().reference.child("Users")
        cardStack()
        cardStackView = view.findViewById(R.id.frame)
        arrayAdapter = ArrayAdapter(rowItem, context, this)
        cardStackView.layoutManager = manager
        cardStackView.adapter = arrayAdapter
        cardStackView.itemAnimator = DefaultItemAnimator()
        like.setOnClickListener(this)
        dislike.setOnClickListener(this)
        touchGps.setOnClickListener(this)
        star.setOnClickListener(this)
        handler = Handler()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        questionViewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return QuestionViewModel(requireContext()) as T
            }
        }).get(QuestionViewModel::class.java)
        questionViewModel.fetchQA.observe(requireActivity(), {
            val dialogFragment: DialogFragment = DialogFragment()
            dialogFragment.setData(it)
            //localizationDelegate.getLanguage(requireContext()).toLanguageTag()
            dialogFragment.show(requireActivity().supportFragmentManager, "example Dialog")
        })
    }

    private fun cardStack() {
        manager = CardStackLayoutManager(context, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardSwiped(direction: Direction?) {
                po = rowItem[manager.topPosition - 1]
                val userId = po.userId!!

                if (direction == Direction.Right) {
                    if (maxLike > 0 || statusVip) {
                        val datetime = hashMapOf<String, Any>()
                        datetime["date"] = ServerValue.TIMESTAMP
                        usersDb.child(userId).child("connection").child("yep").child(currentUid).updateChildren(datetime)
                        maxLike--
                        GlobalVariable.maxLike = maxLike
                        usersDb.child(currentUid).child("MaxLike").setValue(maxLike)
                        isConnectionMatches(userId)
                    } else {
                        handler.postDelayed(Runnable { cardStackView.rewind() }, 200)
                        //DialogAskQuestion(requireContext()).dialogOutOfQuestion().show()
                        questionAskDialog().show()
                        //openDialog()
                    }
                }
                if (direction == Direction.Left) {
                    usersDb.child(userId).child("connection").child("nope").child(currentUid).setValue(true)
                }
                if (direction == Direction.Top) {
                    if (maxStar > 0) {
                        val datetime = hashMapOf<String, Any>()
                        datetime["date"] = ServerValue.TIMESTAMP
                        datetime["super"] = true
                        usersDb.child(userId).child("connection").child("yep").child(currentUid).updateChildren(datetime)
                        usersDb.child(currentUid).child("star_s").child(userId).setValue(true)
                        maxStar--
                        usersDb.child(currentUid).child("MaxStar").setValue(maxStar)
                        isConnectionMatches(userId)
                    } else {
                        handler.postDelayed(Runnable { cardStackView.rewind() }, 200)
                        //questionAskDialog().show()
                        openDialog()
                    }
                }
            }

            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {
                Log.d("ggg", "$position $countLimit $countLimit3 " + rowItem.size)

                if (countLimit2 == 5 && countLimit < countDataSet) {
                    getUser(resultlimit, false, rowItem.size - 1, 5)
                    countLimit2 = 0
                }
                if (countLimit3 % countDataSet == 0 && countLimit3 > 0) {
                    val handler = Handler()
                    handler.postDelayed({
                        callFunctions(countDataSet, false, rowItem.size - 1)
                        countLimit = 0
                        countLimit2 = 0
                    }, 300)

                }
                if (arrayAdapter.itemCount >= 1) {
                    layoutGps.visibility = View.GONE
                }
            }

            override fun onCardDisappeared(view: View?, position: Int) {
                Log.d("ggg", "$position $countLimit $countLimit3 " + rowItem.size)
                if (checkEmpty) {
                    Log.d("ggg2", "$countEmpty $empty")
                    if (countEmpty == empty - 1) {
                        runnable!!.run()
                        layoutGps.visibility = View.VISIBLE
                    }
                    countEmpty++
                }
                countLimit2++
                countLimit3++
            }
        })
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(2)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
    }

    fun createAndLoadRewardedAd(): RewardedAd {
        rewardedAd = RewardedAd(requireActivity(),
                "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                Toast.makeText(requireContext(), "สวย", Toast.LENGTH_SHORT).show()
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                Toast.makeText(requireContext(), errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }

    fun questionAskDialog(): Dialog {
        val view = layoutInflater.inflate(R.layout.question_ask_dialog, null)
        val btnConfirm = view.findViewById<Button>(R.id.confirm_button_askDialog)
        val btnDismiss = view.findViewById<Button>(R.id.dismiss_button_askDialog)
        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window!!.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }
        btnConfirm.setOnClickListener {
            questionViewModel.response(localizationDelegate.getLanguage(requireContext()).toLanguageTag())
            dialog.dismiss()
        }
        return dialog
    }

    fun openDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.vip_dialog, null)
        val b1 = view.findViewById<Button>(R.id.buy)
        val b2 = view.findViewById<Button>(R.id.admob)
        val text = view.findViewById<TextView>(R.id.test_de)
        if (maxAdmob <= 0) {
            text.text = "โฆษณาที่คุณสามารถดูได้ในวันนี้หมดแล้ว \n สมัคร Dessert VIP เพื่อรับสิทธิพิเศษ"
            b2.visibility = View.GONE
        }
        rewardedAd = RewardedAd(requireContext(), "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                b2.text = "ดูโฆษณา"
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        b2.setOnClickListener {
            if (rewardedAd.isLoaded) {
                val activityContext: Activity = requireActivity()
                val adCallback = object : RewardedAdCallback() {
                    override fun onRewardedAdOpened() {
                        rewardedAd = createAndLoadRewardedAd()
                    }

                    override fun onRewardedAdClosed() {

                    }

                    override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                        Log.d("TAG", maxLike.toString())
                        maxLike += 1
                        maxAdmob -= 1
                        if (maxLike >= 10)
                            dialog.dismiss()
                        else if (maxAdmob <= 0) {
                            b2.visibility = View.GONE
                        }

                        usersDb.child(currentUid).child("MaxLike").setValue(maxLike)
                        usersDb.child(currentUid).child("MaxAdmob").setValue(maxAdmob)
                    }

                    override fun onRewardedAdFailedToShow(errorCode: Int) {
                        Log.d("TAG", "The rewarded ad wasn't loaded yet.")
                    }
                }
                rewardedAd.show(activityContext, adCallback)
            } else {
                Log.d("TAG", "The rewarded ad wasn't loaded yet.")
            }
        }
        b1.setOnClickListener {
            bp.subscribe(requireActivity(), "YOUR SUBSCRIPTION ID FROM GOOGLE PLAY CONSOLE HERE");
            usersDb.child(currentUid).child("Vip").setValue(1)
            dialog.dismiss()
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
            requireActivity().startActivity(requireActivity().intent)
            requireActivity().overridePendingTransition(0, 0)
        }
        dialog = Dialog(requireContext())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(view)
        val pagerModels: ArrayList<PagerModel?> = ArrayList()
        pagerModels.add(PagerModel("สมัคร Desert เพื่อกดถูกใจได้ไม่จำกัด ปัดขวาได้เต็มที่ ไม่ต้องรอเวลา", "จำนวนการกดถูกใจของคุณหมด", R.drawable.ic_heart))
        pagerModels.add(PagerModel("คนที่คุณส่งดาวให้จะเห็นคุณก่อนใคร", "รับ 5 Star ฟรีทุกวัน", R.drawable.ic_starss))
        pagerModels.add(PagerModel("สามารถทักทายได้เต็มที ไม่จำกัดจำนวน", "ทักทายได้ไม่จำกัด", R.drawable.ic_hand))
        pagerModels.add(PagerModel("ดูว่าใครบ้างที่เข้ามากดถูกใจให้คุณ", "ใครถูกใจคุณ", R.drawable.ic_love2))
        pagerModels.add(PagerModel("ดูว่าใครบ้างที่เข้าชมโปรไฟล์ของคุณ", "ใครเข้ามาดูโปรไฟล์คุณ", R.drawable.ic_vision))
        val adapter = VipSlide(requireContext(), pagerModels)
        val pager: AutoScrollViewPager = dialog.findViewById(R.id.viewpage)
        pager.adapter = adapter
        pager.startAutoScroll()
        val indicator: CircleIndicator = view.findViewById(R.id.indicator)
        indicator.setViewPager(pager)
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window!!.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    private val runnable: Runnable? = object : Runnable {
        override fun run() {
            anime1.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(800).withEndAction {
                anime1.scaleX = 1f
                anime1.scaleY = 1f
                anime1.alpha = 1f
            }
            anime2.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1200).withEndAction {
                anime2.scaleX = 1f
                anime2.scaleY = 1f
                anime2.alpha = 1f
            }
            handler.postDelayed(this, 1500)
        }
    }

    private fun isConnectionMatches(userId: String) {
        val currentUserConnectionDb = usersDb.child(currentUid).child("connection").child("yep").child(userId)
        currentUserConnectionDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val key = FirebaseDatabase.getInstance().reference.child("Chat").push().key
                    usersDb.child(dataSnapshot.key!!)
                            .child("connection").child("matches")
                            .child(currentUid).child("ChatId")
                            .setValue(key)
                    usersDb.child(currentUid)
                            .child("connection")
                            .child("matches")
                            .child(dataSnapshot.key!!)
                            .child("ChatId").setValue(key)
                    usersDb.child(dataSnapshot.key!!)
                            .child("connection")
                            .child("yep")
                            .child(currentUid)
                            .setValue(null)
                    usersDb.child(currentUid)
                            .child("connection")
                            .child("yep")
                            .child(dataSnapshot.key!!)
                            .setValue(null)
                    if (notificationMatch == "1") {
                        dialog = Dialog(requireContext())
                        val inflater = layoutInflater
                        val view = inflater.inflate(R.layout.show_match, null)
                        val imageView = view.findViewById<ImageView>(R.id.image_match)

                        val textView = view.findViewById<TextView>(R.id.textMatch)
                        val textView2 = view.findViewById<TextView>(R.id.io)
                        val textView4 = view.findViewById<TextView>(R.id.textMatch2)
                        val button = view.findViewById<Button>(R.id.mess)
                        button.setOnClickListener {
                            dialog.dismiss()
                            val intent = Intent(context, ChatActivity::class.java)
                            val b = Bundle()
                            b.putString("matchId", po.userId)
                            b.putString("nameMatch", po.name)
                            b.putString("first_chat", "")
                            b.putString("unread", "0")
                            intent.putExtras(b)
                            requireContext().startActivity(intent)
                        }
                        textView2.setOnClickListener { dialog.dismiss() }
                        textView.text = po.name
                        if (dataSnapshot.hasChild("super")) {
                            star.visibility = View.VISIBLE
                            textView4.text = "  " + "ส่งดาวให้คุณให้คุณ"
                        } else textView4.text = "  " + "ถูกใจคุณเหมือนกัน"
                        Glide.with(requireContext()).load(po.profileImageUrl).into(imageView)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.setContentView(view)
                        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                        dialog.show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getDis() {

        oppositeUserSex = GlobalVariable.oppositeUserSex
        oppositeUserAgeMin = GlobalVariable.oppositeUserAgeMin
        oppositeUserAgeMax = GlobalVariable.oppositeUserAgeMax

        xUser = GlobalVariable.x.toDouble()
        yUser = GlobalVariable.y.toDouble()
        maxLike = GlobalVariable.maxLike
        maxAdmob = GlobalVariable.maxAdmob
        maxStar = GlobalVariable.maxStar
        statusVip = GlobalVariable.vip
        distance = when (GlobalVariable.distance) {
            "true" -> {
                1000.0
            }
            "Untitled" -> {
                1000.0
            }
            else -> {
                GlobalVariable.distance.toDouble()
            }
        }
    }

    private fun callFunctions(limit: Int, type: Boolean, count: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val preferences2 = requireActivity().getSharedPreferences("notification_match", Context.MODE_PRIVATE)
            notificationMatch = preferences2.getString("noti", "1")
            var pre = 0
            if (!type) pre = 0

            val data = hashMapOf(
                    "sex" to oppositeUserSex,
                    "min" to oppositeUserAgeMin,
                    "max" to oppositeUserAgeMax,
                    "x_user" to xUser,
                    "y_user" to yUser,
                    "distance" to distance,
                    "limit" to pre + limit,
                    "prelimit" to pre
            )
            Log.d("tagkl", data.toString())

            functions.getHttpsCallable("getUserCard")
                    .call(data)
                    .addOnFailureListener { Log.d("ghj", "failed") }
                    .addOnSuccessListener { task ->
                        // This continuation runs on either success or failure, but if the task
                        // has failed then result will throw an Exception which will be
                        // propagated down.
                        val result1 = task.data as Map<*, *>
                        resultlimit = result1["o"] as ArrayList<*>
                        if (resultlimit.isNotEmpty()) {
                            Log.d("iii", resultlimit.size.toString())
                            getUser(resultlimit, type, count, 10)
                        } else {
                            val logoMoveAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_out2)
                            val load = (activity as SwitchpageActivity).load
                            logoMoveAnimation.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {

                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    load.visibility = View.GONE
                                }

                                override fun onAnimationRepeat(animation: Animation?) {

                                }
                            })
                            load.startAnimation(logoMoveAnimation)
                            layoutGps.visibility = View.VISIBLE
                            runnable!!.run()
                        }


                    }
        }

    }

    private fun getUser(result2: ArrayList<*>, type: Boolean, count: Int, limit: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Default) {
                val preferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
                val language = preferences.getString("My_Lang", "")
                val ff: Geocoder
                var addresses: MutableList<Address>
                ff = if (language == "th") {
                    Geocoder(context)
                } else {
                    Geocoder(context, Locale.UK)
                }
                Log.d("iop", "")
                var a = countLimit + limit
                if (result2.size < countLimit + limit) {
                    a = result2.size
                    checkEmpty = true
                    empty = result2.size
                }
                for (x in countLimit until a) {
                    countLimit++
                    Log.d("iop", "$countLimit ${result2.size}")
                    val user = result2[x] as Map<*, *>
                    Log.d("ghj", user["name"].toString() + " , " + user["distance_other"].toString())
                    var myself = ""
                    var citysend: String? = ""
                    var offStatus = false
                    var vip = false
                    var starS = false
                    val location = user["Location"] as Map<*, *>
                    try {
                        addresses = ff.getFromLocation(location["X"].toString().toDouble(), location["Y"].toString().toDouble(), 1)
                        val city = addresses[0].adminArea
                        citysend = city
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (user["myself"] != null) {
                        myself = user["myself"].toString()
                    }
                    if (user["off_status"] != null) {
                        offStatus = true
                    }
                    (user["ProfileImage"] as Map<*, *>)["profileImageUrl0"]
                    val profileImageUrl = (user["ProfileImage"] as Map<*, *>)["profileImageUrl0"].toString()

                    var status = "offline"
                    if (user["status"] == 1) {
                        status = "online"
                    }
                    if (user["Vip"] == 1) {
                        vip = true
                    }

                    if (user["star_s"] != null) {
                        if ((user["star_s"] as Map<*, *>)[currentUid] != null)
                            starS = true
                    }
                    dis = df2.format(user["distance_other"])
                    rowItem.add(Cards(user["key"].toString(), user["name"].toString(), profileImageUrl, user["Age"].toString(), dis, citysend, status, myself, offStatus, vip, starS))

                }
            }
            withContext(Dispatchers.Main) {
                if (type) {
                    arrayAdapter.notifyDataSetChanged()
                    val load = (activity as SwitchpageActivity).load
                    CloseLoading(context, load).invoke()

                } else {
                    arrayAdapter.notifyItemRangeChanged(count, rowItem.size)
                }
            }
        }

    }


    private val df2: DecimalFormat = DecimalFormat("#.#")

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1112) {
            handler.postDelayed(Runnable {
                requireActivity().finish()
                requireActivity().overridePendingTransition(0, 0)
                requireActivity().startActivity(requireActivity().intent)
                requireActivity().overridePendingTransition(0, 0)
            }, 400)
        }
        if (requestCode == 115) {
            when (resultCode) {
                1 -> likeDelay()
                2 -> disLikeDelay()
                3 -> starDelay()
            }
        }

    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requireActivity().recreate()
                getDis()
            }
        }
    }*/

    override fun onStart() {
        super.onStart()
        Log.d("4581", this::manager.isInitialized.toString())


    }

    private fun likeDelay() {
        val handler = Handler()
        handler.postDelayed({
            like()
        }, 300)

    }

    private fun like() {
        val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
        manager.setSwipeAnimationSetting(setting)
        cardStackView.swipe()
    }

    private fun disLikeDelay() {
        val handler = Handler()
        handler.postDelayed({
            disLike()
        }, 300)
    }

    private fun disLike() {
        val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
        manager.setSwipeAnimationSetting(setting)
        cardStackView.swipe()
    }


    private fun starDelay() {
        val handler = Handler()
        handler.postDelayed({
            star()
        }, 300)

    }

    private fun star() {
        val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Top)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
        manager.setSwipeAnimationSetting(setting)
        cardStackView.swipe()
    }

    override fun onBillingInitialized() {

    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {

    }

    override fun onDestroy() {
        bp.release()
        super.onDestroy()
    }

    private fun checkStart() {
        /*mLocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf<String?>(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET
            ), 1)
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, this)*/
        viewLifecycleOwner.lifecycleScope.launch { // launch a new coroutine in background and continue
            withContext(Dispatchers.Default) { // background thread
                getDis()
            }
            withContext(Dispatchers.IO) { // background thread
                callFunctions(countDataSet, true, 0)
            }
        }

        //}
    }

    override fun onClick(v: View?) {
        if (v == touchGps) {
            startActivityForResult(Intent(context, Setting2Activity::class.java), 1112)
        }
        if (v == like) {
            like()
        }
        if (v == dislike) {
            disLike()
        }
        if (v == star) {
            star()
        }
    }

}