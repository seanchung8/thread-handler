package edu.cascadia.brianb.threadhandler;

import android.app.Activity;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* Code example based on http://www.techotopia.com/index.php/A_Basic_Overview_of_Android_Threads_and_Thread_handlers
 */
public class ThreadExample extends Activity {

    int numThreads;
    TextView threadCounterView, myTextView;
    Handler mHandler;

    //TODO: define mHandler as an anonymous class and override handleMessage to use msg data to update the UI
    //TODO: increment and decrement numThreads counter

    private static class MyHandler extends Handler {
        private final WeakReference<ThreadExample> mActivity;

        public MyHandler(ThreadExample activity) {
            super();
            mActivity = new WeakReference<ThreadExample>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String text = msg.getData().getString("myKey");
            ThreadExample activity = mActivity.get();

            activity.updateTextView(text);

            activity.updateThreadCountTextView();

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_example);
        threadCounterView = (TextView) findViewById(R.id.threadCount);
        myTextView = (TextView) findViewById(R.id.myTextView);

        /*mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = msg.getData().getString("myKey");

                myTextView.setText(text);

                threadCounterView.setText("Thread Count: " + decrementThreadCount());
            }
        };*/
        mHandler = new MyHandler(this);
    }

    // increment numTheards thread-safe
    private synchronized int incrementThreadCount() {
        return ++numThreads;
    }

    // decrement numTheards thread-safe
    private synchronized int decrementThreadCount() {
        return --numThreads;
    }

    public void buttonClick(View view) {
        //Create a new thread to do the time consuming operation
        Thread timeLapse = new Thread(new Runnable() {
            @Override
            public void run() {

                //This is where the time goes while the thread is running
                takeSomeTime(5);

                //Send a message to the UI Thread through a Handler
                if (mHandler != null) {
                    // need to decrement the thread count separately for
                    // MyHandler class.
                    decrementThreadCount();

                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    SimpleDateFormat dateformat =
                            new SimpleDateFormat("HH:mm:ss", Locale.US);
                    String dateString = dateformat.format(new Date());
                    bundle.putString("myKey", "It's now: " + dateString);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            }
        });
        timeLapse.start();

        myTextView.setText("This might take a moment...");
        threadCounterView.setText("Thread Count: " + incrementThreadCount());
    }

    // Mimic time delay in a network activity
    public void takeSomeTime(int seconds) {
        long endTime = System.currentTimeMillis() + seconds * 1000;

        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    System.out.println(">>>Setting wait for thread #" + numThreads);
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }
        System.out.println(">>>takeSomeTime completed for thread #" + numThreads);

    }

    public void updateTextView(String text) {
        myTextView.setText(text);
    }

    public void updateThreadCountTextView() {
        threadCounterView.setText("Thread Count: " + numThreads);
    }
}


