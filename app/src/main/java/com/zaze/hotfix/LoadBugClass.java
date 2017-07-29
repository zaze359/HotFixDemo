package com.zaze.hotfix;

/**
 * Description :
 *
 * @author : ZAZE
 * @version : 2017-07-26 - 10:33
 */
public class LoadBugClass {

    public String getBugString() {
        BugClass bugClass = new BugClass();
        return bugClass.bug();
    }
}
