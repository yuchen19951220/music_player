package com.android.lib_network.okhttp.response;

        import android.content.Intent;
        import android.os.Handler;
        import android.os.Looper;
        import android.os.Message;

        import com.android.lib_network.okhttp.exception.OkHttpException;
        import com.android.lib_network.okhttp.listener.DisposeDataHandle;
        import com.android.lib_network.okhttp.listener.DisposeDownloadListener;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;

        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.Response;

/**
 * 处理文件下载反馈
 */
public class CommonFileCallback implements Callback {
    //定义信息常量
    /**
     * the java layer exception, do not same to the logic error
     */
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int IO_ERROR = -2; // the JSON relative error
    protected final String EMPTY_MSG = "";
    /**
     * 将其它线程的数据转发到UI线程
     */
    private static final int PROGRESS_MESSAGE = 0x01;

    private Handler mDeliveryHandler;
    private DisposeDownloadListener mListener;
    private String mFilePath;
    private int mProgress;

    public CommonFileCallback(DisposeDataHandle handle){
        this.mListener=(DisposeDownloadListener) handle.mListener;
        this.mFilePath=handle.mSource;
        this.mDeliveryHandler =new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case PROGRESS_MESSAGE:
                        //回调函数 监挺数据传输过程
                        mListener.onProgress((int)msg.obj);
                        break;
                }
            }
        };
    }

    @Override
    public void onFailure(Call call, final IOException e) {
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onFailure(new OkHttpException(NETWORK_ERROR,e));
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        final File file =handleResponse(response);
        //交给子线程处理
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                //回调逻辑
                if (file!=null){
                    mListener.onSuccess(file);
                } else {
                    mListener.onFailure(new OkHttpException(IO_ERROR, EMPTY_MSG));
                }
            }
        });
    }

    private File handleResponse(Response response) {
        if (response==null) {
            return null;
        }
        InputStream inputStream=null;
        File file=null;
        FileOutputStream fos=null;
        byte[] buffer=new byte[2048];
        int length;
        double sumLength;
        double currentLength=0;

        //判断文件是否存在
        try {
            checkLocalFilePath(mFilePath);
            file=new File(mFilePath);
            fos=new FileOutputStream(file);
            inputStream=response.body().byteStream();
            sumLength=response.body().contentLength();
            while ((length=inputStream.read(buffer))!=-1){
                fos.write(buffer,0,length);
                currentLength+=length;
                mProgress=(int)(currentLength/sumLength)*100;//当前进度
                mDeliveryHandler.obtainMessage(PROGRESS_MESSAGE,mProgress);//对外发送一个进度事件
            }
            fos.flush();
        } catch (IOException e) {
            file=null;
        } finally {
            try {
                if (fos!=null)
                fos.close();
                if (inputStream!=null)
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 查看是否存在当前路径文件
     * @param mFilePath
     */
    private void checkLocalFilePath(String mFilePath) {
        //提取路径
        File path=new File(mFilePath.substring(0,mFilePath.lastIndexOf("/")+1));

        File file=new File(mFilePath);
        //如果路径不存在 创建路径
        if (!path.exists()){
            path.mkdir();
        }
        //如果文件不存在 创建文件
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
