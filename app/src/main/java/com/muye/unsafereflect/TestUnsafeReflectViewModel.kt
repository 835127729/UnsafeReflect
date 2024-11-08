package com.muye.unsafereflect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.muye.unsafe_reflect.UnsafeReflect
import java.io.File
import java.lang.invoke.MethodHandles


class TestUnsafeReflectViewModel : ViewModel() {
    companion object {
        private const val TAG = "TestUnsafeReflectViewMo"
    }

    fun testReflectField(context: Context): Boolean = kotlin.runCatching {
        //get
        //public static
        if (UnsafeReflect.getValue(View::class.java, null, "NO_ID") != -1) {
            Log.e(TAG, "testReflectField: NO_ID != -1")
            return@runCatching false
        }
        //protected static
        if (UnsafeReflect.getValue(View::class.java, null, "VIEW_LOG_TAG") != "View") {
            Log.e(TAG, "testReflectField: VIEW_LOG_TAG != View")
            return@runCatching false
        }
        //private static
        if (UnsafeReflect.getValue(View::class.java, null, "VISIBILITY_FLAGS") == null) {
            Log.e(TAG, "testReflectField: VISIBILITY_FLAGS == null")
            return@runCatching false
        }

        val linearLayout = LinearLayout(context)
        //public
        if (UnsafeReflect.getValue(View::class.java, linearLayout, "mCachingFailed") == null) {
            Log.e(TAG, "testReflectField: mCachingFailed is null")
            return@runCatching false
        }
        //protected
        if (UnsafeReflect.getValue(View::class.java, linearLayout, "mContext") != context) {
            Log.e(TAG, "testReflectField: mContext != $context")
            return@runCatching false
        }
        //private
        if (UnsafeReflect.getValue(
                View::class.java,
                linearLayout,
                "mResources"
            ) != context.resources
        ) {
            Log.e(TAG, "testReflectField: mResources != $context.resources")
            return@runCatching false
        }

        //set
        //static
        //private
        UnsafeReflect.setValue(LinearLayout::class.java, null, "VERTICAL_GRAVITY_COUNT", 5)
        try {
            if (UnsafeReflect.getValue(
                    LinearLayout::class.java,
                    null,
                    "VERTICAL_GRAVITY_COUNT"
                ) != 5
            ) {
                Log.e("TAG, ", "VERTICAL_GRAVITY_COUNT is not 5")
                return@runCatching false
            }
        } finally {
            UnsafeReflect.setValue(LinearLayout::class.java, null, "VERTICAL_GRAVITY_COUNT", 4)
        }

        //public
        UnsafeReflect.setValue(LinearLayout::class.java, null, "VERTICAL", 2)
        try {
            if (LinearLayout::class.java.getDeclaredField("VERTICAL").get(null) != 2) {
                Log.e(TAG, "testReflectField: VERTICAL != 2")
                return@runCatching false
            }
        } finally {
            UnsafeReflect.setValue(LinearLayout::class.java, null, "VERTICAL", 1)
        }
        //protected
        val origin = UnsafeReflect.getValue(View::class.java, null, "EMPTY_STATE_SET")
        UnsafeReflect.setValue(View::class.java, null, "EMPTY_STATE_SET", null)
        try {
            if (View::class.java.getDeclaredField("EMPTY_STATE_SET").run {
                    isAccessible = true
                    get(null)
                } != null) {
                Log.e(TAG, "testReflectField: EMPTY_STATE_SET is not null")
                return@runCatching false
            }
        } finally {
            UnsafeReflect.setValue(View::class.java, null, "EMPTY_STATE_SET", origin)
        }

        //instance
        //public
        UnsafeReflect.setValue(View::class.java, linearLayout, "mCachingFailed", true)
        if (UnsafeReflect.getValue(
                View::class.java,
                linearLayout,
                "mCachingFailed"
            ) != true
        ) {
            Log.e(TAG, "testReflectField: mCachingFailed != true")
            return@runCatching false
        }

        //protected
        UnsafeReflect.setValue(View::class.java, linearLayout, "mContext", null)
        if (linearLayout.context != null) {
            Log.e(TAG, "testReflectField: mContext != null")
            return@runCatching false
        }
        //private
        UnsafeReflect.setValue(
            LinearLayout::class.java,
            linearLayout,
            "mOrientation",
            LinearLayout.VERTICAL
        )
        if (linearLayout.orientation != LinearLayout.VERTICAL) {
            Log.e(TAG, "testReflectField: mOrientation != LinearLayout.VERTICAL")
            return@runCatching false
        }

        //fields
        val staticFields = UnsafeReflect.getStaticFields(LinearLayout::class.java)
        if (staticFields.isEmpty()) {
            Log.e(TAG, "testReflectField: staticFields.isEmpty()")
            return@runCatching false
        }
        if (UnsafeReflect.getStaticField(View::class.java, "VISIBILITY_FLAGS") == null) {
            Log.e(TAG, "testReflectField: VISIBILITY_FLAGS == null")
            return@runCatching false
        }
        val instanceFields = UnsafeReflect.getInstanceFields(LinearLayout::class.java)
        if (instanceFields.isEmpty()) {
            Log.e(TAG, "testReflectField: instanceFields.isEmpty()")
            return@runCatching false
        }
        if (UnsafeReflect.getInstanceField(View::class.java, "mContext") == null) {
            Log.e(TAG, "testReflectField: mContext == null")
            return@runCatching false
        }

        true
    }.getOrDefault(false)

    fun testReflectMethod(context: Context): Boolean = kotlin.runCatching {
        //constructor
        val view =
            UnsafeReflect.newInstance(View::class.java, arrayOf(Context::class.java), context)
        if (view == null) {
            Log.w(TAG, "testReflectMethod: view is null")
            return@runCatching false
        }
        val linearLayout = UnsafeReflect.newInstance(
            LinearLayout::class.java,
            arrayOf(Context::class.java),
            context
        )
        if (linearLayout == null) {
            Log.w(TAG, "testReflectMethod: linearLayout is null")
            return@runCatching false
        }

        val linearLayout2 = UnsafeReflect.newInstance(
            LinearLayout::class.java,
            arrayOf(Context::class.java, AttributeSet::class.java),
            context, null
        )
        if (linearLayout2 == null) {
            Log.w(TAG, "testReflectMethod: linearLayout2 is null")
            return@runCatching false
        }

        //static
        //public
        val root = UnsafeReflect.invoke(
            View::class.java,
            null,
            "inflate",
            arrayOf(Context::class.java, Int::class.java, ViewGroup::class.java),
            context,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            null
        ) as View?
        if (root == null) {
            Log.w(TAG, "testReflectMethod: root is null")
            return@runCatching false
        }
        //protected
        if (UnsafeReflect.invoke(
                View::class.java, null, "debugIndent", arrayOf(Int::class.java), 1
            ) == null
        ) {
            Log.w(TAG, "testReflectMethod: debugIndent is null")
            return@runCatching false
        }
        //private
        if (UnsafeReflect.invoke(
                View::class.java,
                null,
                "printFlags",
                arrayOf(Int::class.java),
                View.INVISIBLE
            ) == null
        ) {
            Log.w(TAG, "testReflectMethod: printFlags is null")
            return@runCatching false
        }

        //instance
        val contextFromView = UnsafeReflect.invoke(View::class.java, linearLayout, "getContext")
        //public
        if (contextFromView != context) {
            Log.w(TAG, "testReflectMethod fail, context not match")
            return@runCatching false
        }
        //private
        if (UnsafeReflect.invoke(View::class.java, linearLayout, "getFinalAlpha") == null) {
            Log.w(TAG, "testReflectMethod fail, getFinalAlpha is null")
            return@runCatching false
        }
        //protected
        if (UnsafeReflect.invoke(View::class.java, linearLayout, "awakenScrollBars") == null) {
            Log.w(TAG, "testReflectMethod fail, awakenScrollBars is null")
            return@runCatching false
        }

        //methods
        val staticMethods = UnsafeReflect.getStaticMethods(View::class.java)
        if (staticMethods.isEmpty()) {
            Log.w(TAG, "testReflectMethod fail, staticMethods is empty")
            return@runCatching false
        }
        val instanceMethods = UnsafeReflect.getInstanceMethods(View::class.java)
        if (instanceMethods.isEmpty()) {
            Log.w(TAG, "testReflectMethod fail, instanceMethods is empty")
            return@runCatching false
        }
        if (UnsafeReflect.getStaticMethod(
                View::class.java,
                "printFlags",
                arrayOf(Int::class.java)
            ) == null
        ) {
            Log.w(TAG, "testReflectMethod fail, printFlags is null")
            return@runCatching false
        }
        if (UnsafeReflect.getInstanceMethod(
                View::class.java,
                "getFinalAlpha",
            ) == null
        ) {
            Log.w(TAG, "testReflectMethod fail, getFinalAlpha is null")
            return@runCatching false
        }
        true
    }.getOrDefault(false)

    @SuppressLint("SoonBlockedPrivateApi", "PrivateApi")
    fun testHiddenApi(): Boolean {
        Log.d(TAG, "testApi() called")
        return runCatching {
            val clazz = Class.forName("android.webkit.WebViewLibraryLoader")
            val loadNativeLibraryMethod = clazz.getDeclaredMethod(
                "loadNativeLibrary",
                ClassLoader::class.java,
                String::class.java
            )
            loadNativeLibraryMethod.invoke(null, null, "webviewchromium") as Int
            true
        }.onFailure {
            Log.e(TAG, "loadNativeLibrary: ", it)
        }.onSuccess {
            Log.d(TAG, "loadNativeLibrary result: $it")
        }.getOrDefault(false)
    }

    fun bypassTestApi(): Boolean {
        Log.d(TAG, "testApi() called")
        return runCatching {
            val clazz = Class.forName("android.webkit.WebViewLibraryLoader")
            val res = UnsafeReflect.invoke(
                clazz, null, "loadNativeLibrary", arrayOf(
                    ClassLoader::class.java,
                    String::class.java
                ), null, "webviewchromium"
            ) as Int
            Log.i(TAG, "loadNativeLibrary: res = $res")
            true
        }.onFailure {
            Log.e(TAG, "loadNativeLibrary: ", it)
        }.onSuccess {
            Log.d(TAG, "loadNativeLibrary result: $it")
        }.getOrDefault(false)
    }

    fun bypassTestApi2(): Boolean {
        Log.d(TAG, "testApi() called")
        return runCatching {
            val clazz = Class.forName("android.webkit.WebViewLibraryLoader")
            val res = UnsafeReflect.invoke(
                clazz, null, "loadNativeLibrary", arrayOf(
                    ClassLoader::class.java,
                    String::class.java
                ), null, "webviewchromium"
            ) as Int
            Log.i(TAG, "loadNativeLibrary: res = $res")
            true
        }.onFailure {
            Log.e(TAG, "loadNativeLibrary: ", it)
        }.onSuccess {
            Log.d(TAG, "loadNativeLibrary result: $it")
        }.getOrDefault(false)
    }

    private fun testAndroidBug(context: Context, creator: (ParcelFileDescriptor) -> PdfRenderer?) {
        //Create a damaged PDF file
        val file = File.createTempFile("temp", "pdf")
        runCatching {
            //Creating a PdfRenderer object through this file will throw an exception in Android 7.1 and below, but it can be captured
            val pdf =
                creator(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            pdf?.close()
        }
        //Create a normal PDF file
        val pdfFile = File.createTempFile("test", "pdf")
        pdfFile.outputStream().use {
            context.resources.openRawResource(R.raw.test).apply {
                copyTo(it)
                close()
            }
        }
        //Creating a PdfRenderer object again will crash and fail to capture it
        val pdf2 =
            creator(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
        if (pdf2 != null) {
            val page = pdf2.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pdf2.close()
        }
    }

    fun invokeAndroidBug(context: Context) {
        testAndroidBug(context) {
            PdfRenderer(it)
        }
    }

    fun fixAndroidBug(context: Context) {
        testAndroidBug(context) {
            createPdfRender(it)
        }
    }

    private fun createPdfRender(input: ParcelFileDescriptor): PdfRenderer? {
        var nativeCreateFail = false
        var pdfRenderer: PdfRenderer? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            pdfRenderer = runCatching {
                //create PdfRenderer without calling constructor
                val o = UnsafeReflect.allocateInstance(PdfRenderer::class.java)
                //mTempPoint
                UnsafeReflect.setValue(PdfRenderer::class.java, o, "mTempPoint", Point())
                //mCloseGuard
                val clazzCloseGuard = Class.forName("dalvik.system.CloseGuard")
                val mCloseGuard =
                    UnsafeReflect.invoke(clazzCloseGuard, null, "get")
                UnsafeReflect.setValue(PdfRenderer::class.java, o, "mCloseGuard", mCloseGuard)

                //mock constructor
                //Libcore.os.lseek(input.getFileDescriptor(), 0, OsConstants.SEEK_SET);
                Os.lseek(input.fileDescriptor, 0, OsConstants.SEEK_SET)
                //size = Libcore.os.fstat(input.getFileDescriptor()).st_size;
                val size = Os.fstat(input.fileDescriptor).st_size
                //mNativeDocument = nativeCreate(mInput.getFd(), size);
                val mNativeDocument = UnsafeReflect.invoke(
                    PdfRenderer::class.java, o, "nativeCreate", arrayOf(
                        Int::class.java, Long::class.java
                    ), input.fd, size
                ) as Long?
                if (mNativeDocument == null) {
                    /**
                     * below android8, If the nativeCreate call fails, the PdfRenderer will be closed in the constructor;
                     * however, when the finalize method is called, the PdfRenderer will be closed repeatedly.
                     * Set mInput to null to avoid this.
                     */
                    nativeCreateFail = true
                    /**
                     * need to close input to prevent memory leak
                     */
                    UnsafeReflect.invoke(
                        ParcelFileDescriptor::class.java,
                        input,
                        "close"
                    )
                    return null
                }
                //mInput = input
                UnsafeReflect.setValue(PdfRenderer::class.java, o, "mInput", input)
                //mNativeDocument = mInput.getFd();
                UnsafeReflect.setValue(
                    PdfRenderer::class.java,
                    o,
                    "mNativeDocument",
                    mNativeDocument
                )
                //mPageCount = nativeGetPageCount(mNativeDocument);
                val mPageCount =
                    UnsafeReflect.invoke(
                        PdfRenderer::class.java, o, "nativeGetPageCount", arrayOf(
                            Long::class.java
                        ), mNativeDocument
                    ) as Int?
                UnsafeReflect.setValue(
                    PdfRenderer::class.java, o, "mPageCount", mPageCount
                )
                //mCloseGuard.open("close");
                UnsafeReflect.invoke(
                    clazzCloseGuard,
                    mCloseGuard,
                    "open",
                    arrayOf(String::class.java),
                    "close"
                )
                Log.d(TAG, "create PdfRenderer by unsafe successfully")
                o
            }.onFailure {
                Log.e(TAG, "$it")
            }.getOrNull()
        }

        /**
         * Below android O, if PdfRender fails to be created through unsafe, then follow the normal creation logic
         */
        if (pdfRenderer == null && !nativeCreateFail) {
            pdfRenderer = runCatching {
                PdfRenderer(input)
            }.onFailure {
                Log.e(TAG, "$it")
            }.onSuccess {
                Log.d(TAG, "new PdfRenderer successfully")
            }.getOrNull()
        }
        return pdfRenderer;
    }
}