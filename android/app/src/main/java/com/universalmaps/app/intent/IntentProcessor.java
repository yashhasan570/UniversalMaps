package com.universalmaps.app.intent;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.universalmaps.app.MwmActivity;

public interface IntentProcessor
{
  /** @return true if the intent was handled and no further processors should run. */
  boolean process(@NonNull Intent intent, @NonNull MwmActivity activity);
}
