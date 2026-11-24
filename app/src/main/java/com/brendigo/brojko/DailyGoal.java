package com.brendigo.brojko;

/** Daily goal artwork identity is independent of language. */
enum DailyGoal {
    TILE(R.string.daily_goal_tile_title,R.string.daily_goal_tile_description),
    POINTS(R.string.daily_goal_points_title,R.string.daily_goal_points_description),
    MOVES(R.string.daily_goal_moves_title,R.string.daily_goal_moves_description);

    final int titleResource;
    final int descriptionResource;

    DailyGoal(int titleResource,int descriptionResource){
        this.titleResource=titleResource;
        this.descriptionResource=descriptionResource;
    }
}
