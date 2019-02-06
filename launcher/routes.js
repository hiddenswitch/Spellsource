/**
 * Created by bberman on 6/15/17.
 */

FlowRouter.route('/', {
    action: function () {
        if (Electron.isDesktop()) {
            BlazeLayout.render("layout", {content: 'launcher'});
        } else {
            BlazeLayout.render("layout", {content: 'upload'});
        }
    }
});