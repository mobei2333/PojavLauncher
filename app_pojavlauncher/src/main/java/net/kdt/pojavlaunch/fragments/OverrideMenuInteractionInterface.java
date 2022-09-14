package net.kdt.pojavlaunch.fragments;

public interface OverrideMenuInteractionInterface {

    /** @return Whether the fragment can be removed by the home button on the activity */
    boolean allowHome();

    /** @return Whether the fragment can be removed from the screen by a back button */
    boolean allowBackButton();
}
