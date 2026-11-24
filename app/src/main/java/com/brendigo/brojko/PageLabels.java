package com.brendigo.brojko;

/** Stable navigation IDs and their localized accessibility labels. */
final class PageLabels {
    private PageLabels(){}
    static int resource(String target,TileGame game){
        switch(target){
            case "game": return game==null?R.string.mode_classic:
                "daily".equals(game.mode)?R.string.mode_daily:
                "relaxed".equals(game.mode)?R.string.mode_relaxed:R.string.mode_classic;
            case "pause": return R.string.pause_title;
            case "win": return R.string.victory_bravo;
            case "over": return R.string.game_over_title;
            case "dailyWin": return R.string.daily_success;
            case "dailyFail": return R.string.daily_failure;
            case "confirm": return R.string.confirm_new;
            case "confirmEnd": return R.string.confirm_end;
            case "daily": return R.string.mode_daily;
            case "missions": return R.string.home_missions;
            case "stats": return R.string.home_statistics;
            case "historyDetail": return R.string.history_detail;
            case "themes": return R.string.themes_heading;
            case "settings": return R.string.settings_title;
            case "language": return R.string.settings_language;
            case "backup": return R.string.backup_title;
            case "help": return R.string.help_title;
            case "about": return R.string.about_title;
            case "privacy": return R.string.privacy_title;
            default: return R.string.home_title;
        }
    }
}
