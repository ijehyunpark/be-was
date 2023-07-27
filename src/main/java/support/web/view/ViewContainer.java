package support.web.view;

import support.annotation.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Container
public class ViewContainer {

    private final List<View> viewContainer = new ArrayList<>();

    public ViewContainer() {
        viewContainer.add(new ErrorView());
        viewContainer.add(new IndexView());
        viewContainer.add(new PostShowView());
        viewContainer.add(new UserListView());
    }

    public View getViewByName(String name) {
        return viewContainer.stream()
                .filter(view -> Objects.equals(view.getName(), name))
                .findAny()
                .orElse(null);
    }

}
