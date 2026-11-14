package com.universalmaps.app.editor;

import androidx.fragment.app.Fragment;
import com.universalmaps.app.base.BaseMwmFragmentActivity;

public class ProfileActivity extends BaseMwmFragmentActivity
{
  public static final String EXTRA_REDIRECT_TO_PROFILE = "redirectToProfile";

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return ProfileFragment.class;
  }
}
