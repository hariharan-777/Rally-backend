package com.userservice.User.model;

/**
 * Sports a player can be rated in. Ratings and Elo are tracked separately per sport,
 * because being great at badminton says little about your tennis.
 */
public enum Sport {
    BADMINTON,
    TENNIS,
    PICKLEBALL,
    TABLE_TENNIS,
    SQUASH,
    PADEL,
    OTHER
}
