/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fairphone.peaceofmind;

import android.content.Context;

public class AirplaneModeDeviceController implements IDeviceController
{

    private Context mContext;

    AirplaneModeDeviceController(Context context)
    {

        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }

        mContext = context;
    }

    @Override
    public void startPeaceOfMind()
    {

        if (!AirplaneModeToggler.isAirplaneModeOn(mContext))
        {
             AirplaneModeToggler.setAirplaneModeOn(mContext);
        }
    }

    @Override
    public void endPeaceOfMind()
    {

        if (AirplaneModeToggler.isAirplaneModeOn(mContext))
        {
             AirplaneModeToggler.setAirplaneModeOff(mContext);
        }
    }

    @Override
    public void screenOffDevice()
    {

    }
}
