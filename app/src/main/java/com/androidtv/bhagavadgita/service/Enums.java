package com.androidtv.bhagavadgita.service;

import android.os.Bundle;

import androidx.media3.session.CommandButton;
import androidx.media3.session.SessionCommand;

public class Enums {

//    public static final String CUSTOM_COMMAND_REWIND_ACTION_ID = "REWIND_15";
//    public static final String CUSTOM_COMMAND_FORWARD_ACTION_ID = "FAST_FWD_15";
    public static final String CUSTOM_COMMAND_FAVORITE_ACTION_ID = "FAVORITE";

    public enum NotificationPlayerCustomCommandButton {
//        REWIND(
//                CUSTOM_COMMAND_REWIND_ACTION_ID,
//                new CommandButton.Builder()
//                        .setDisplayName("Rewind")
//                        .setSessionCommand(new SessionCommand(CUSTOM_COMMAND_REWIND_ACTION_ID, new Bundle()))
//                        .setIconResId(androidx.media3.session.R.drawable.media3_icon_skip_back)
//                        .build()
//        ),
//        FORWARD(
//                CUSTOM_COMMAND_FORWARD_ACTION_ID,
//                new CommandButton.Builder()
//                        .setDisplayName("Forward")
//                        .setSessionCommand(new SessionCommand(CUSTOM_COMMAND_FORWARD_ACTION_ID, new Bundle()))
//                        .setIconResId(androidx.media3.session.R.drawable.media3_icon_skip_forward)
//                        .build()
//        ),
        FAVORITE(
                CUSTOM_COMMAND_FAVORITE_ACTION_ID,
                new CommandButton.Builder()
                        .setDisplayName("Favorite")
                        .setSessionCommand(new SessionCommand(CUSTOM_COMMAND_FAVORITE_ACTION_ID, new Bundle()))
                .setIconResId(androidx.media3.session.R.drawable.media3_icon_heart_unfilled)
                        .build()
        );

        public final String customAction;
        private final CommandButton commandButton;

        NotificationPlayerCustomCommandButton(String customAction, CommandButton commandButton) {
            this.customAction = customAction;
            this.commandButton = commandButton;
        }

        public String getCustomAction() {
            return customAction;
        }

        public CommandButton getCommandButton() {
            return commandButton;
        }
    }
}