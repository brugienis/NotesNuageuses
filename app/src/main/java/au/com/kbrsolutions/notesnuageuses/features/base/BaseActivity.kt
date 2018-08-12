package au.com.kbrsolutions.notesnuageuses.features.base

//import android.support.test.orchestrator.junit.BundleJUnitUtils.getResult
//import org.junit.experimental.results.ResultMatchers.isSuccessful

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import au.com.kbrsolutions.notesnuageuses.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource


abstract class BaseActivity: AppCompatActivity() {

    private val TAG = "BaseActivity"

    /**
     * Request code for Google Sign-in
     */
    protected val REQUEST_CODE_SIGN_IN = 0

    /**
     * Request code for the Drive picker
     */
    protected val REQUEST_CODE_OPEN_ITEM = 1

    /**
     * Handles high-level drive functions like sync
     */
    private var mDriveClient: DriveClient? = null

    /**
     * Handle access to Drive resources/files.
     */
    private var mDriveResourceClient: DriveResourceClient? = null

    /**
     * Tracks completion of the drive picker
     */
    private var mOpenItemTaskSource: TaskCompletionSource<DriveId>? = null

    override fun onStart() {
        super.onStart()
        signIn()
    }

    /**
     * Handles resolution callbacks.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                if (resultCode != Activity.RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Log.e(TAG, "Sign-in failed.")
                    finish()
                    return
                }

                val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult())
                } else {
                    Log.e(TAG, "Sign-in failed.")
                    finish()
                }
            }
            REQUEST_CODE_OPEN_ITEM -> if (resultCode == Activity.RESULT_OK) {
                val driveId: DriveId = data.getParcelableExtra(
                        OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
                mOpenItemTaskSource!!.setResult(driveId)
            } else {
                mOpenItemTaskSource!!.setException(RuntimeException("Unable to open file"))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    protected fun signIn() {
        val requiredScopes = mutableSetOf<Scope>()
        requiredScopes.add(Drive.SCOPE_FILE)
        requiredScopes.add(Drive.SCOPE_APPFOLDER)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signInAccount != null && signInAccount!!.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount)
        } else {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Drive.SCOPE_FILE)
                    .requestScopes(Drive.SCOPE_APPFOLDER)
                    .build()
            val googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN)
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        mDriveClient = Drive.getDriveClient(applicationContext, signInAccount)
        mDriveResourceClient = Drive.getDriveResourceClient(applicationContext, signInAccount)
        onDriveClientReady()
    }

    /**
     * Prompts the user to select a text file using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected fun pickTextFile(): Task<DriveId>? {
        val openOptions = OpenFileActivityOptions.Builder()
                .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .setActivityTitle(getString(R.string.select_file))
                .build()
        return pickItem(openOptions)
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected fun pickFolder(): Task<DriveId>? {
        val openOptions = OpenFileActivityOptions.Builder()
                .setSelectionFilter(
                        Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                .setActivityTitle(getString(R.string.select_folder))
                .build()
        return pickItem(openOptions)
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private fun pickItem(openOptions: OpenFileActivityOptions): Task<DriveId>? {
        mOpenItemTaskSource = TaskCompletionSource()
        getDriveClient()!!
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith({ task: Task<IntentSender> ->
                    startIntentSenderForResult(
                            task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0)
                    null
                } as Continuation<IntentSender, Void>)
        return mOpenItemTaskSource!!.getTask()
    }

    /**
     * Shows a toast message.
     */
    protected fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    abstract protected fun onDriveClientReady()

    protected fun getDriveClient(): DriveClient? {
        return mDriveClient
    }

    protected fun getDriveResourceClient(): DriveResourceClient? {
        return mDriveResourceClient
    }
}