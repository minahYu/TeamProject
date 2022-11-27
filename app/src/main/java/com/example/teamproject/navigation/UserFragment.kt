package com.example.teamproject.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teamproject.LoginActivity
import com.example.teamproject.MainActivity
import com.example.teamproject.R
import com.example.teamproject.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.example.teamproject.navigation.model.ContentDTO
import com.example.teamproject.navigation.model.FollowDTO
import com.example.teamproject.navigation.util.FcmPush
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    //val binding by lazy { FragmentUserBinding.inflate(layoutInflater) }
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null
    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid) {
            // 내 페이지
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            var mainActivity = (activity as MainActivity)
            mainActivity?.binding.toolbarUsername?.text = auth?.currentUser?.email
            mainActivity?.binding.toolbarBtnBack?.setOnClickListener {
                mainActivity?.binding.bottomNavigation.selectedItemId = R.id.action_home
            }
            mainActivity?.binding.toolbarTitleImage?.visibility = View.GONE
            mainActivity?.binding.toolbarUsername?.visibility = View.VISIBLE
            mainActivity?.binding.toolbarBtnBack?.visibility = View.GONE
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            // 다른 사람 페이지
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainActivity = (activity as MainActivity)
            mainActivity?.binding.toolbarUsername?.text = arguments?.getString("userId")
            mainActivity?.binding.toolbarBtnBack?.setOnClickListener {
                mainActivity?.binding.bottomNavigation.selectedItemId = R.id.action_home
            }
            mainActivity?.binding.toolbarTitleImage?.visibility = View.GONE
            mainActivity?.binding.toolbarUsername?.visibility = View.VISIBLE
            mainActivity?.binding.toolbarBtnBack?.visibility = View.VISIBLE
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(requireActivity(),3)

        fragmentView?.account_recyclerview?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }

    fun getFollowerAndFollowing() {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { value, error ->
            if(value == null)
                return@addSnapshotListener
            var followDTO = value.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null) {
                fragmentView?.account_tv_following_count?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null) {
                fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount?.toString()
                if(followDTO?.followers?.containsKey(currentUserUid!!)) {
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.unfollow)
                    fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                } else {
                    //fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                    if(uid != currentUserUid) {
                        fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                    }
                }
            }
        }
    }

    fun requestFollow() {
        // save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)) {
                // It remove following third person when a third person follow me
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers?.remove(uid)
            } else {
                if(fragmentView?.account_btn_follow_signout?.text == "Follow") {
                    // It add following third person when a third person do not follow me
                    followDTO?.followingCount = followDTO?.followingCount + 1
                    followDTO?.followers[uid!!] = true
                }
                if(fragmentView?.account_btn_follow_signout?.text == "UnFollow") {
                    followDTO?.followingCount = followDTO?.followingCount -1
                    followDTO?.followers?.remove(uid)
                }
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        // save data to third person
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)

            if(followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if(followDTO!!.followers.containsKey(currentUserUid)) {
                // It cancel my follower when I don't follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            }else {
                // It add my follower when I don't follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    fun followerAlarm(destinationUid: String) {
        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        FcmPush.instance.sendMessage(destinationUid, "Dear Diary", message)
    }

    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { value, error ->
            if(value == null)
                return@addSnapshotListener
            if(value.data != null) {
                var url = value?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { value, error ->
            if(value == null)
                return@addSnapshotListener

                // get data
                for(snapshot in value.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) :
            RecyclerView.ViewHolder(imageView) {
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.imageView.context).load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }

}