package com.example.teamproject.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teamproject.R
import com.example.teamproject.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.teamproject.databinding.FragmentDetailBinding
import com.example.teamproject.navigation.model.AlarmDTO
import com.example.teamproject.navigation.util.FcmPush
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid = FirebaseAuth.getInstance().currentUser?.uid
    //val binding by lazy {FragmentDetailBinding.inflate(layoutInflater)}
    //var currentUserUid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { value, error ->
                    contentDTOs.clear()
                    contentUidList.clear()

                    // Sometimes, This code return null of querySnapshot when it signout
                    if (value == null)
                        return@addSnapshotListener

                    for (snapshot in value!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            // UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![position].userId

            // Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.detailviewitem_imageview_content)

            // Explain of content
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![position].explain

            // likes
            viewholder.detailviewitem_favoritecounter_textview.text =
                "Likes " + contentDTOs!![position].favoriteCount

            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(contentDTOs[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(viewholder.detailviewitem_profile)
                    }
                }

            // This code is when the page is loaded
            if (contentDTOs!![position].favorites.containsKey(uid)) {
                // This is like status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_heart)
            } else {
                // This is unlike status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_heart_border)
            }

            // 상대방 프사 클릭 시 상대방 정보로 넘어감
            viewholder.detailviewitem_profile.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }
        }

        fun favoriteEvent(position : Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)) {
                    // 버튼 눌려있을 때
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                } else { // 눌려있지 않을 때
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc,contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid, "Dear Diary", message) // dear diary 수정하기
        }
    }
}













