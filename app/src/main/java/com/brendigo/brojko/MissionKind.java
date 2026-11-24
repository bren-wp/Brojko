package com.brendigo.brojko;

/** Stable mission identity, never derived from translated copy. */
enum MissionKind {
    MERGE(R.string.mission_merge_title,R.string.mission_merge_description),
    NO_UNDO(R.string.mission_no_undo_title,R.string.mission_no_undo_description),
    STREAK(R.string.mission_streak_title,R.string.mission_streak_description),
    WEEK_SCORE(R.string.mission_week_score_title,R.string.mission_week_score_description),
    MASTER(R.string.mission_master_title,R.string.mission_master_description),
    KING(R.string.mission_king_title,R.string.mission_king_description);

    final int titleResource;
    final int descriptionResource;

    MissionKind(int titleResource,int descriptionResource){
        this.titleResource=titleResource;
        this.descriptionResource=descriptionResource;
    }
}
