package com.patriciasoco.myopinion.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.patriciasoco.myopinion.R
import java.util.concurrent.TimeUnit


class LoginFragment : Fragment() {
    private lateinit var signInRequest : BeginSignInRequest
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var signInIntent : Intent
    private lateinit var oneTapClient: SignInClient
    private lateinit var storedVerificationId : String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var  editTextPhone : EditText
    private lateinit var  sendcodeButton : Button
    private lateinit var  entercode : EditText
    private lateinit var  validateButton : Button
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
        // Initialize Firebase Auth
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this.requireActivity())
        functions = Firebase.functions

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso);
        signInIntent = mGoogleSignInClient.signInIntent
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id)
                    ).setFilterByAuthorizedAccounts(true)
                    .build())
            .build();

        val currentUser = auth.currentUser
        if(currentUser != null){
            showmyprofile (currentUser)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val registerButton = this.requireView().findViewById<Button>(R.id.registerButton)
        val editTextEmail = this.requireView().findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = this.requireView().findViewById<EditText>(R.id.editTextPassword)
        val loginButton = this.requireView().findViewById<Button>(R.id.loginButton)
        val loginGoogleButton = this.requireView().findViewById<Button>(R.id.loginGoogle)
        sendcodeButton = this.requireView().findViewById<Button>(R.id.sendcode)
        editTextPhone = this.requireView().findViewById<EditText>(R.id.editTextPhone)
        entercode = this.requireView().findViewById<EditText>(R.id.entercode)
        validateButton = this.requireView().findViewById<Button>(R.id.validateButton)

        entercode.visibility = View.INVISIBLE
        validateButton.visibility = View.INVISIBLE

        validateButton.setOnClickListener{
            var code = entercode.text.toString()
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
        sendcodeButton.setOnClickListener{
            var phone = "+52" + editTextPhone.text.toString()
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this.requireActivity())                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        registerButton.setOnClickListener{
            var email = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("CreateUser", "createUserWithEmail:success")
                        val user = auth.currentUser
                        sendemailverification (user!!)
                        Toast.makeText(this.requireContext(), "We have sent a verification email",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("CreateUser", "createUserWithEmail:failure", task.exception)
                        Log.d("Create User", task.exception?.message!!)
                        if (task.exception?.message!!.contains("INVALID_ARGUMENT")){
                            Toast.makeText(this.requireContext(), "We have sent a verification email",
                                Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(
                                this.requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }
        loginButton.setOnClickListener{
            var email = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LoginUser", "loginUserWithEmail:success")
                        val user = auth.currentUser
                        showmyprofile (user!!)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginUser", "loginUserWithEmail:failure", task.exception)
                        Toast.makeText(this.requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        loginGoogleButton.setOnClickListener{

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    activityResultLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                }
                .addOnFailureListener { e -> // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d("Google singIn", e.localizedMessage)
                }

        }
    }
    private var activityResultLauncher = registerForActivityResult(
        StartIntentSenderForResult()) { result ->
        Log.d("sigingoogle", "" + result.resultCode)
        Log.d("sigingoogle", "" + result.data)
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent = result.data!!
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = googleCredential.googleIdToken
                firebaseAuthWithGoogle(idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("SingIn Google", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle (idToken : String) {
        when {
            idToken != null -> {

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("sigingoogle", "signInWithCredential:success")
                            val user = auth.currentUser
                            showmyprofile (user!!)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("sigingoogle", "signInWithCredential:failure", task.exception)
                            Toast.makeText(this.requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else -> {
                // Shouldn't happen.
                Log.d("sigingoogle", "No ID token!")
            }
        }
    }
    var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("phoneautenthication", "onVerificationCompleted:$credential")

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("phoneautenthication", "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("phoneautenthication", "onCodeSent:$verificationId")
            editTextPhone.visibility = View.GONE
            sendcodeButton.visibility = View.GONE
            entercode.visibility = View.VISIBLE
            validateButton.visibility = View.VISIBLE


            showAlert()

            storedVerificationId = verificationId
            resendToken = token
        }
    }

    fun showAlert(){
        Toast.makeText(this.requireContext(), "Code sent",
            Toast.LENGTH_SHORT).show()
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signinwithphone", "signInWithCredential:success")
                   val user =auth.currentUser
                    showmyprofile (user!!)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("signinwithphone", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this.requireContext(), "Invalid Code.",
                            Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }
    fun showmyprofile (user: FirebaseUser){
        user.getIdToken(true).addOnSuccessListener { result ->
            Log.d("custom claims", result.claims.toString())
            val isAdmin: Boolean = result.claims ["admin"] as Boolean
            val isHost: Boolean = result.claims["Host"] as Boolean

            if (isAdmin) {
                val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, MyProfileAdmin())
                transaction.disallowAddToBackStack()
                transaction.commit()
            } else if (isHost){
                val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, MyProfileHost())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }
            else {
                val transaction = this.requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, MyProfileFragment())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }


        }
    }

    fun sendemailverification (user: FirebaseUser){
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Email verification", "Email sent.")
                    confirmemailsent ()
                }
            }
    }
    private fun confirmemailsent (): Task<String> {


        return functions
            .getHttpsCallable("confirmemailsent")

            .call()
            .continueWith { task ->
                val result = task.result?.data as String
                Firebase.auth.signOut()
                result
            }
    }

}