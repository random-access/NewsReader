package org.random_access.newsreader;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        //formUri = "http://www.backendofyourchoice.com/reportpath"
        mailTo = "reports@random-access.org",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text,
        logcatArguments = { "-t", "100", "-v", "long", "ActivityManager:I", "NewsReader:D", "*:S" }
)

/**
 * <b>Project:</b> NewsReader for Android <br>
 * <b>Date:</b> 05.09.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NewsReader extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

}
