package com.universalmaps.app.help;

import androidx.fragment.app.Fragment;
import com.universalmaps.app.base.BaseToolbarActivity;

public class HelpActivity extends BaseToolbarActivity
{
  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return HelpFragment.class;
  }
}
