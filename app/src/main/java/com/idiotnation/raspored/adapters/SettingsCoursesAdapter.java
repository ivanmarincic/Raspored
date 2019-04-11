package com.idiotnation.raspored.adapters;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseTypeDto;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsCoursesAdapter extends RecyclerView.Adapter<SettingsCoursesAdapter.ViewHolder> {

    private List<CourseTypeDto> list;
    private ItemOnSelectListener listener;
    private Integer selectedItem;
    private Integer filteredOutItem;
    private Integer selectedBackground;

    public List<CourseTypeDto> getList() {
        return list;
    }

    public void setList(List<CourseTypeDto> list) {
        this.list = list;
    }

    public SettingsCoursesAdapter(Context context, List<CourseTypeDto> list, Integer selectedItem, Integer filteredOutItem) {
        this.list = list;
        this.selectedItem = selectedItem;
        this.filteredOutItem = filteredOutItem;
        selectedBackground = ContextCompat.getColor(context, R.color.colorOnBackgroundSelected);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_view_course_selection_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final CourseTypeDto item = list.get(position);
        holder.title.setText(item.getName());
        holder.isCollapsed = true;
        holder.list.getLayoutParams().height = 0;
        holder.list.requestLayout();
        if (item.getCourses() != null) {
            for (int i = 0; i < item.getCourses().size(); i++) {
                final CourseDto subItem = item.getCourses().get(i);
                if (subItem.getId().equals(filteredOutItem)) {
                    continue;
                }
                ListViewHolder subHolder = new ListViewHolder(LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.settings_view_course_selection_list_item, holder.list, false));
                subHolder.value.setText(subItem.getName().replace(item.getName(), "").trim());
                if (subItem.getId().equals(selectedItem)) {
                    subHolder.itemView.setBackgroundColor(selectedBackground);
                } else {
                    subHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }
                subHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onSelect(subItem);
                        }
                    }
                });
                holder.list.addView(subHolder.itemView);
            }
        }
        holder.header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.isCollapsed) {
                    expand(holder);
                } else {
                    collapse(holder);
                }
            }
        });
    }

    public static void expand(ViewHolder holder) {
        final View v = holder.list;
        final View arrow = holder.arrow;
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        ValueAnimator va = ValueAnimator.ofInt(1, targetHeight);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                v.requestLayout();
                if (targetHeight > 0) {
                    arrow.setRotation((Integer) animation.getAnimatedValue() / targetHeight * 180);
                } else {
                    arrow.setRotation(180);
                }
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        va.setDuration(400);
        va.setInterpolator(new OvershootInterpolator());
        va.start();
        holder.isCollapsed = false;
    }

    public static void collapse(ViewHolder holder) {
        final View v = holder.list;
        final View arrow = holder.arrow;
        final int initialHeight = v.getMeasuredHeight();

        ValueAnimator va = ValueAnimator.ofInt(initialHeight, 1);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                v.requestLayout();
                if (initialHeight > 0) {
                    arrow.setRotation((Integer) animation.getAnimatedValue() / initialHeight * 180);
                } else {
                    arrow.setRotation(0);
                }
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        va.setDuration(400);
        va.setInterpolator(new DecelerateInterpolator());
        va.start();
        holder.isCollapsed = true;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View header;
        AppCompatTextView title;
        AppCompatImageView arrow;
        LinearLayout list;
        boolean isCollapsed = true;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.settings_view_course_selection_item_header);
            title = itemView.findViewById(R.id.settings_view_course_selection_item_header_title);
            arrow = itemView.findViewById(R.id.settings_view_course_selection_item_arrow);
            list = itemView.findViewById(R.id.settings_view_course_selection_item_list);
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView value;

        ListViewHolder(@NonNull View itemView) {
            super(itemView);
            value = itemView.findViewById(R.id.list_item_value);
        }
    }

    public interface ItemOnSelectListener {
        void onSelect(CourseDto item);
    }

    public void setItemOnSelectListener(ItemOnSelectListener listener) {
        this.listener = listener;
    }
}
