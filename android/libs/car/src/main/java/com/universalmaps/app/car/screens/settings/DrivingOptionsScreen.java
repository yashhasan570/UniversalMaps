package com.universalmaps.app.car.screens.settings;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.car.app.CarContext;
import androidx.car.app.model.Action;
import androidx.car.app.model.Header;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.car.app.model.Toggle;
import androidx.car.app.navigation.model.MapWithContentTemplate;
import androidx.lifecycle.LifecycleOwner;
import com.universalmaps.app.car.R;
import com.universalmaps.app.car.util.UiHelpers;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.Router;
import com.universalmaps.app.sdk.car.renderer.Renderer;
import com.universalmaps.app.sdk.car.screens.BaseMapScreen;
import com.universalmaps.app.sdk.routing.RoutingOptions;
import com.universalmaps.app.sdk.settings.RoadType;
import java.util.HashMap;
import java.util.Map;

public class DrivingOptionsScreen extends BaseMapScreen
{
  public static final Object DRIVING_OPTIONS_RESULT_CHANGED = 0x1;

  private record DrivingOption(RoadType roadType, @StringRes int text)
  {
  }

  private final DrivingOption[] mDrivingOptions = {new DrivingOption(RoadType.Toll, R.string.avoid_tolls),
                                                   new DrivingOption(RoadType.Dirty, R.string.avoid_unpaved),
                                                   new DrivingOption(RoadType.Ferry, R.string.avoid_ferry),
                                                   new DrivingOption(RoadType.Motorway, R.string.avoid_motorways)};

  @NonNull
  private final Map<RoadType, Boolean> mInitialDrivingOptionsState = new HashMap<>();

  public DrivingOptionsScreen(@NonNull CarContext carContext, @NonNull UniversalMaps universalMapsContext,
                              @NonNull Renderer surfaceRenderer)
  {
    super(carContext, universalMapsContext, surfaceRenderer);

    initDrivingOptionsState();
  }

  @NonNull
  @Override
  protected Template onGetTemplateImpl()
  {
    final MapWithContentTemplate.Builder builder = new MapWithContentTemplate.Builder();
    builder.setMapController(UiHelpers.createMapController(getCarContext(), getSurfaceRenderer(), getLocationHelper()));
    builder.setContentTemplate(createDrivingOptionsListTemplate());
    return builder.build();
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner)
  {
    super.onStop(owner);
    for (final DrivingOption drivingOption : mDrivingOptions)
    {
      if (Boolean.TRUE.equals(mInitialDrivingOptionsState.get(drivingOption.roadType))
          != RoutingOptions.hasOption(Router.Vehicle, drivingOption.roadType))
      {
        setResult(DRIVING_OPTIONS_RESULT_CHANGED);
        return;
      }
    }
  }

  @NonNull
  private Header createHeader()
  {
    final Header.Builder builder = new Header.Builder();
    builder.setStartHeaderAction(Action.BACK);
    builder.setTitle(getCarContext().getString(R.string.driving_options_title));
    return builder.build();
  }

  @NonNull
  private ListTemplate createDrivingOptionsListTemplate()
  {
    final ItemList.Builder builder = new ItemList.Builder();
    for (final DrivingOption drivingOption : mDrivingOptions)
      builder.addItem(createDrivingOptionsToggle(drivingOption.roadType, drivingOption.text));
    return new ListTemplate.Builder().setHeader(createHeader()).setSingleList(builder.build()).build();
  }

  @NonNull
  private Row createDrivingOptionsToggle(RoadType roadType, @StringRes int title)
  {
    final Toggle.OnCheckedChangeListener listener = (isChecked) ->
    {
      if (isChecked)
        RoutingOptions.addOption(Router.Vehicle, roadType);
      else
        RoutingOptions.removeOption(Router.Vehicle, roadType);
      invalidate();
    };
    final Row.Builder toggle = new Row.Builder();
    toggle.setTitle(getCarContext().getString(title));
    toggle.setToggle(
        new Toggle.Builder(listener).setChecked(RoutingOptions.hasOption(Router.Vehicle, roadType)).build());
    return toggle.build();
  }

  private void initDrivingOptionsState()
  {
    for (final DrivingOption drivingOption : mDrivingOptions)
      mInitialDrivingOptionsState.put(drivingOption.roadType,
                                      RoutingOptions.hasOption(Router.Vehicle, drivingOption.roadType));
  }
}
