package com.universalmaps.app.car.screens;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.Header;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;
import com.universalmaps.app.MwmApplication;
import com.universalmaps.app.R;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.car.screens.BaseScreen;
import com.universalmaps.app.sdk.display.DisplayType;

public class MapPlaceholderScreen extends BaseScreen
{
  public MapPlaceholderScreen(@NonNull CarContext carContext, @NonNull UniversalMaps universalMapsContext)
  {
    super(carContext, universalMapsContext);
  }

  @NonNull
  @Override
  protected Template onGetTemplateImpl()
  {
    final MessageTemplate.Builder builder =
        new MessageTemplate.Builder(getCarContext().getString(R.string.car_used_on_the_phone_screen));

    final Header.Builder headerBuilder = new Header.Builder();
    headerBuilder.setStartHeaderAction(Action.APP_ICON);
    headerBuilder.setTitle(getCarContext().getString(com.universalmaps.app.branding.R.string.app_name));
    builder.setHeader(headerBuilder.build());
    builder.setIcon(
        new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_phone_android)).build());
    builder.addAction(
        new Action.Builder()
            .setTitle(getCarContext().getString(R.string.car_continue_in_the_car))
            .setOnClickListener(
                () -> MwmApplication.from(getCarContext()).getDisplayManager().changeDisplay(DisplayType.Car))
            .build());

    return builder.build();
  }
}
