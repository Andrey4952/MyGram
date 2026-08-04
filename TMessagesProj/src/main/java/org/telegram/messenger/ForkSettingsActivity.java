package org.telegram.messenger;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Objects;

public class ForkSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter adapter;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.ForkSettingsTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutAnimation(null);
        listView.setAdapter(adapter = new ListAdapter());
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setDurations(350);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setSupportsChangeAnimations(false);
        listView.setItemAnimator(itemAnimator);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= items.size()) {
                return;
            }
            ItemInner item = items.get(position);
            if (item.id == 1) {
                SharedConfig.ghostMode = !SharedConfig.ghostMode;
                ((TextCheckCell) view).setChecked(SharedConfig.ghostMode);
                SharedConfig.saveConfig();
            } else if (item.id == 3) {
                SharedConfig.keepDeleted = !SharedConfig.keepDeleted;
                ((TextCheckCell) view).setChecked(SharedConfig.keepDeleted);
                SharedConfig.saveConfig();
            } else if (item.id == 5) {
                SharedConfig.keepDeletedInBots = !SharedConfig.keepDeletedInBots;
                ((TextCheckCell) view).setChecked(SharedConfig.keepDeletedInBots);
                SharedConfig.saveConfig();
            } else if (item.id == 7) {
                SharedConfig.showKeptDeleted = !SharedConfig.showKeptDeleted;
                ((TextCheckCell) view).setChecked(SharedConfig.showKeptDeleted);
                SharedConfig.saveConfig();
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
            } else if (item.id == 100) {
                Browser.openUrl(getParentActivity(), Uri.parse(LocaleController.getString(R.string.ForkSourceCodeUrl)));
            } else if (item.id == 101) {
                Browser.openUrl(getParentActivity(), Uri.parse(LocaleController.getString(R.string.ForkTelegramChannelUrl)));
            }
        });

        updateItems(false);

        return fragmentView;
    }

    private final ArrayList<ItemInner> oldItems = new ArrayList<>(), items = new ArrayList<>();

    private void updateItems(boolean animated) {
        oldItems.clear();
        oldItems.addAll(items);
        items.clear();

        items.add(new ItemInner(VIEW_TYPE_HEADER, 0, LocaleController.getString(R.string.ForkPrivacy)));
        items.add(new ItemInner(VIEW_TYPE_CHECK, 1, LocaleController.getString(R.string.ForkGhostMode)));
        items.add(new ItemInner(VIEW_TYPE_SHADOW, 2, LocaleController.getString(R.string.ForkGhostModeDesc)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, 3, LocaleController.getString(R.string.ForkKeepDeleted)));
        items.add(new ItemInner(VIEW_TYPE_SHADOW, 4, LocaleController.getString(R.string.ForkKeepDeletedDesc)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, 5, LocaleController.getString(R.string.ForkKeepDeletedBots)));
        items.add(new ItemInner(VIEW_TYPE_SHADOW, 6, LocaleController.getString(R.string.ForkKeepDeletedBotsDesc)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, 7, LocaleController.getString(R.string.ForkShowKeptDeleted)));
        items.add(new ItemInner(VIEW_TYPE_SHADOW, 8, LocaleController.getString(R.string.ForkShowKeptDeletedDesc)));

        items.add(new ItemInner(VIEW_TYPE_HEADER, 100, LocaleController.getString(R.string.ForkAbout)));
        items.add(new ItemInner(VIEW_TYPE_LINK, 100, "─── " + LocaleController.getString(R.string.ForkSourceCode) + " ───"));
        items.add(new ItemInner(VIEW_TYPE_LINK, 101, "─── " + LocaleController.getString(R.string.ForkTelegramChannel) + " ───"));

        if (adapter == null) {
            return;
        }

        if (animated) {
            adapter.setItems(oldItems, items);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private final static int VIEW_TYPE_HEADER = 0;
    private final static int VIEW_TYPE_CHECK = 1;
    private final static int VIEW_TYPE_SHADOW = 2;
    private final static int VIEW_TYPE_LINK = 3;

    private static class ItemInner extends AdapterWithDiffUtils.Item {
        public CharSequence text;
        public int id;
        public ItemInner(int viewType, int id, CharSequence text) {
            super(viewType, false);
            this.id = id;
            this.text = text;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemInner item = (ItemInner) o;
            return id == item.id && Objects.equals(text, item.text);
        }
    }

    private class ListAdapter extends AdapterWithDiffUtils {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_HEADER) {
                view = new HeaderCell(getContext());
            } else if (viewType == VIEW_TYPE_CHECK) {
                view = new TextCheckCell(getContext());
            } else if (viewType == VIEW_TYPE_LINK) {
                view = new TextInfoPrivacyCell(getContext());
            } else {
                view = new TextInfoPrivacyCell(getContext());
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < 0 || position >= items.size()) {
                return;
            }
            ItemInner item = items.get(position);
            final boolean divider = position + 1 < items.size() && items.get(position + 1).viewType == item.viewType;
            if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(item.text);
            } else if (holder.getItemViewType() == VIEW_TYPE_SHADOW) {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                if (TextUtils.isEmpty(item.text)) {
                    cell.setFixedSize(12);
                    cell.setText(null);
                } else {
                    cell.setFixedSize(0);
                    cell.setText(item.text);
                }
            } else if (holder.getItemViewType() == VIEW_TYPE_CHECK) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                boolean checked;
                if (item.id == 1) {
                    checked = SharedConfig.ghostMode;
                } else if (item.id == 3) {
                    checked = SharedConfig.keepDeleted;
                } else if (item.id == 5) {
                    checked = SharedConfig.keepDeletedInBots;
                } else if (item.id == 7) {
                    checked = SharedConfig.showKeptDeleted;
                } else {
                    return;
                }
                cell.setTextAndCheck(item.text, checked, divider);
            } else if (holder.getItemViewType() == VIEW_TYPE_LINK) {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setFixedSize(dp(48));
                cell.setText(item.text);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int t = holder.getItemViewType();
            return t == VIEW_TYPE_CHECK || t == VIEW_TYPE_LINK;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position >= items.size()) {
                return 0;
            }
            return items.get(position).viewType;
        }
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        listView.setPadding(0, 0, 0, bottom);
        listView.setClipToPadding(false);
    }
}
