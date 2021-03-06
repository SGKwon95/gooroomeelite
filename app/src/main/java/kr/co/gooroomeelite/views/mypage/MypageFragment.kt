package kr.co.gooroomeelite.views.mypage

/**
 * @author Gnoss
 * @email silmxmail@naver.com
 * @created 2021-06-08
 * @desc
 */

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.co.gooroomeelite.BuildConfig
import kr.co.gooroomeelite.R
import kr.co.gooroomeelite.databinding.FragmentMypageBinding
import kr.co.gooroomeelite.model.ContentDTO
import kr.co.gooroomeelite.utils.LoginUtils.Companion.isLogin
import org.w3c.dom.Document
import java.io.File

class MypageFragment(val owner:AppCompatActivity) : Fragment() {

    private lateinit var binding : FragmentMypageBinding

    var storage : FirebaseStorage? = null
    var auth : FirebaseAuth? = null
    var storageRef : StorageReference? = null
    val version = BuildConfig.VERSION_NAME
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var photoUri : String?= null
    var nickname : String?=null
    var email : String?=null

    companion object {
        fun newInstance(owner: AppCompatActivity) : Fragment {
            return MypageFragment(owner)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        uid = arguments?.getString("destinationUid")
        binding = FragmentMypageBinding.inflate(inflater,container,false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mypage,container,false)
        binding.my = this
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        email = auth!!.currentUser?.email
        owner.setSupportActionBar(binding.toolbar2)
        setting()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        getImageNickName()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(owner.supportActionBar) {
            this!!.setDisplayHomeAsUpEnabled(true)
            setTitle("Gooroomee")
        }

        //??????????????????
        binding.btnLogout.setOnClickListener {
            val mLogoutView =
                LayoutInflater.from(owner).inflate(R.layout.fragment_dialog_logout, null)
            val mBuilder = androidx.appcompat.app.AlertDialog.Builder(owner).setView(mLogoutView)
            val mAlertDialog = mBuilder.show().apply {
                window?.setBackgroundDrawable(null)
            }
            val okButton = mLogoutView.findViewById<Button>(R.id.btn_logout_ok)
            val cancelButton = mLogoutView.findViewById<Button>(R.id.btn_logout_no)


            okButton.setOnClickListener {
                //????????????

                if(isLogin()) {
                    FirebaseAuth.getInstance().signOut()
                }

                //?????????????????? ????????????//


                Toast.makeText(owner,"???????????????????????????.",Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
            }
            cancelButton.setOnClickListener {
                Toast.makeText(owner, "?????????????????????.", Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
            }
        }


        //???????????? ????????????


        //????????????
        binding.btnProfileAccount.setOnClickListener {
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            val intent01 = Intent(owner,ProfileAccountActivity::class.java)
            intent01.putExtra("destinationUid",uid)
            startActivity(intent01)
        }

        binding.btnTermsOfService.setOnClickListener {
            val intent02 = Intent(owner,TermsOfServiceActivity::class.java)
            startActivity(intent02)
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            val intent03 = Intent(owner,PrivacyPolicyActivity::class.java)
            startActivity(intent03)
        }

        binding.btnOpenSource.setOnClickListener {
            val intent04 = Intent(owner,OpenSourceActivity::class.java)
            startActivity(intent04)
        }

        binding.btnWithdrawal.setOnClickListener {
            val intent05 = Intent(owner,WithdrawalActivity::class.java)
            startActivity(intent05)
        }
    }

    private fun getImageNickName(){
        var file : File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/profile_img")
        if(file?.isDirectory == null){
            file?.mkdir()
        }
        else
        downloadImgNickName()
    }
    private fun downloadImgNickName(){
        val num = uid
        var filename = "profile$num.jpg"
//        firestore?.collection("users")?.document(uid!!)?.get()?.addOnSuccessListener { ds ->
//            val nickname = ds.data?.get("nickname").toString()
//            binding.nickname.text = nickname
//        }
        storage = FirebaseStorage.getInstance()
        storageRef = storage!!.reference
        storageRef!!.child("profile_img/$filename").child(filename).downloadUrl.addOnSuccessListener{
            Glide.with(owner).load(it).into(binding.imageView)
        }

    }
    private fun setting(){
        firestore!!.collection("users").whereEqualTo("userId",email).get().addOnSuccessListener { ds ->
            val contentDTO = ds.toObjects(ContentDTO::class.java)
            val nickname = contentDTO[0].nickname
            val email = contentDTO[0].userId
            binding.emailaddress.text=email
            binding.nickname.text=nickname
        }
    }
}

