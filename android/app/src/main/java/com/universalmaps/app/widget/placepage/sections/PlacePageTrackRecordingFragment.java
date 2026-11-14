package com.universalmaps.app.widget.placepage.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.universalmaps.app.R;
import com.universalmaps.app.location.TrackRecordingService;
import com.universalmaps.app.sdk.bookmarks.data.ElevationInfo;
import com.universalmaps.app.sdk.bookmarks.data.MapObject;
import com.universalmaps.app.sdk.bookmarks.data.TrackRecording;
import com.universalmaps.app.sdk.bookmarks.data.TrackStatistics;
import com.universalmaps.app.sdk.location.TrackRecorder;
import com.universalmaps.app.sdk.util.StringUtils;
import com.universalmaps.app.util.UiUtils;
import com.universalmaps.app.util.Utils;
import com.universalmaps.app.widget.placepage.ElevationProfileViewRenderer;
import com.universalmaps.app.widget.placepage.PlacePageViewModel;

public class PlacePageTrackRecordingFragment extends Fragment
{
  private PlacePageViewModel mViewModel;
  private ElevationProfileViewRenderer mElevationProfileViewRenderer;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    mViewModel = new ViewModelProvider(requireActivity()).get(PlacePageViewModel.class);
    return inflater.inflate(R.layout.placepage_track_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    mElevationProfileViewRenderer = new ElevationProfileViewRenderer(view.findViewById(R.id.elevation_profile));
    TrackRecordingService.getStatistics().observe(getViewLifecycleOwner(), trackStatistics -> {
      if (trackStatistics != null)
        render(trackStatistics);
    });
  }

  private void render(@NonNull TrackStatistics trackStatistics)
  {
    if (!TrackRecorder.nativeIsTrackRecordingEnabled())
      return;

    MapObject mo = mViewModel.getMapObject().getValue();
    if (mo == null || !mo.isTrackRecording())
      return;
    mViewModel.setTrackRecordingSaveVisible(!TrackRecorder.nativeIsTrackRecordingEmpty());
    ((TrackRecording) mo)
        .setTrackRecordingPPDescription(
            StringUtils.nativeFormatDistance(trackStatistics.getLength()).toString(requireContext()) + " • "
            + Utils.formatDuration(requireContext(), (int) trackStatistics.getDuration(), R.dimen.text_size_body_3));
    ElevationInfo elevationInfo = TrackRecorder.nativeGetElevationInfo();
    // elevationInfo can be null when Track Recording has just started and no elevation data exists yet.
    if (elevationInfo != null)
    {
      mElevationProfileViewRenderer.render(/* track */ null, elevationInfo, trackStatistics);
      UiUtils.show(requireView());
    }
    else
      UiUtils.hide(requireView());
  }
}
