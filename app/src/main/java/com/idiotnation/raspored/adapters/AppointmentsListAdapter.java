package com.idiotnation.raspored.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.card.MaterialCardView;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.custom.HeaderItemDecoration;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.models.dto.AppointmentEmptyDto;
import com.idiotnation.raspored.models.dto.AppointmentHeaderDto;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface {

    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_ITEM_EMPTY = 1;
    public static final int VIEW_TYPE_HEADER = 2;

    private Integer indexOfNow = -1;
    private List<AppointmentDto> list;
    private Context context;
    private SparseArray<Object> sparseList;
    private SparseIntArray sparseListTypes;
    private List<Integer> headerPositions;
    private ItemOnSelectListener listener;
    private int colorPrimary;
    private int colorOnPrimary;
    private int colorBackgroundVerLight;
    private int elevation;

    public void setList(List<AppointmentDto> list) {
        sparseList = new SparseArray<>();
        sparseListTypes = new SparseIntArray();
        headerPositions = new ArrayList<>();
        this.list = list;
        int index = 0;
        DateTime startDate = DateTime.now().withZone(DateTimeZone.getDefault());
        if (list.size() > 0) {
            DateTime firstStart = list.get(0).getStart();
            if (firstStart.weekOfWeekyear().get() > startDate.weekOfWeekyear().get()) {
                startDate = firstStart.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
            }
        }
        for (int dayOfWeek = DateTimeConstants.MONDAY; dayOfWeek < DateTimeConstants.MONDAY + DateTimeConstants.DAYS_PER_WEEK; dayOfWeek++) {
            boolean wasThisDay = false;
            sparseList.put(index, new AppointmentHeaderDto(Utils.getDayOfWeekString(dayOfWeek, context), startDate.withDayOfWeek(dayOfWeek).toDateTime()));
            sparseListTypes.put(index, VIEW_TYPE_HEADER);
            headerPositions.add(index);
            if (startDate.getDayOfWeek() == dayOfWeek) {
                indexOfNow = index;
            }
            index++;
            for (AppointmentDto appointment : list) {
                if (dayOfWeek == appointment.getStart().getDayOfWeek()) {
                    wasThisDay = true;
                    sparseList.put(index, appointment);
                    sparseListTypes.put(index, VIEW_TYPE_ITEM);
                    index++;
                } else if (wasThisDay) {
                    break;
                }
            }
            if (!wasThisDay) {
                sparseList.put(index, new AppointmentEmptyDto(context.getResources().getString(R.string.main_view_appointments_list_empty_day)));
                sparseListTypes.put(index, VIEW_TYPE_ITEM_EMPTY);
                index++;
            }
        }
    }

    public Integer getIndexOfNow() {
        return indexOfNow;
    }

    public AppointmentsListAdapter(Context context, List<AppointmentDto> list) {
        this.context = context;
        setList(list);
        colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary);
        colorOnPrimary = ContextCompat.getColor(context, R.color.colorOnPrimary);
        colorBackgroundVerLight = ContextCompat.getColor(context, R.color.colorBackgroundVeryLight);
        elevation = context.getResources().getDimensionPixelSize(R.dimen.header_elevation);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                return new ViewHolderItem(inflater.inflate(R.layout.main_view_appointments_list_item, parent, false));
            case VIEW_TYPE_ITEM_EMPTY:
                return new ViewHolderItemEmpty(inflater.inflate(R.layout.main_view_appointments_list_item_empty, parent, false));
            case VIEW_TYPE_HEADER:
                return new ViewHolderHeader(inflater.inflate(R.layout.main_view_appointments_list_header, parent, false));
            default:
                return null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItem) {
            final AppointmentDto item = (AppointmentDto) sparseList.get(position);
            ((ViewHolderItem) holder).name.setText(item.getName() + (item.getDetails() != null && item.getDetails().length() > 0 ? ", " + item.getDetails() : ""));
            ((ViewHolderItem) holder).classroom.setText(item.getClassroom());
            ((ViewHolderItem) holder).time.setText(item.getStart().withZone(DateTimeZone.getDefault()).toString("HH:mm") + " - " + item.getEnd().withZone(DateTimeZone.getDefault()).toString("HH:mm"));
            ((ViewHolderItem) holder).container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSelect(item);
                    }
                }
            });
            return;
        }
        if (holder instanceof ViewHolderHeader) {
            AppointmentHeaderDto item = (AppointmentHeaderDto) sparseList.get(position);
            ((ViewHolderHeader) holder).name.setText(item.getValue());
            return;
        }
        if (holder instanceof ViewHolderItemEmpty) {
            AppointmentEmptyDto item = (AppointmentEmptyDto) sparseList.get(position);
            ((ViewHolderItemEmpty) holder).text.setText(item.getValue());
        }
    }

    @Override
    public int getItemCount() {
        return sparseList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return sparseListTypes.get(position);
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int lastPosition = 0;
        for (int position : headerPositions) {
            if (itemPosition >= position) {
                lastPosition = position;
            } else {
                return lastPosition;
            }
        }
        return lastPosition;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.main_view_appointments_list_header_date;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        AppCompatTextView date = header.findViewById(R.id.main_view_appointments_list_header_date);
        MaterialCardView dateContainer = header.findViewById(R.id.main_view_appointments_list_header_date_container);
        AppointmentHeaderDto item = (AppointmentHeaderDto) sparseList.get(headerPosition);
        date.setText(item.getDate().toLocalDateTime().toString("dd\nMM"));
        if (headerPosition == indexOfNow) {
            dateContainer.setCardBackgroundColor(colorPrimary);
            dateContainer.setCardElevation(elevation);
            dateContainer.setRadius(dateContainer.getHeight() / 2);
            date.setTextColor(colorOnPrimary);
        } else {
            dateContainer.setCardBackgroundColor(Color.TRANSPARENT);
            dateContainer.setCardElevation(0);
            dateContainer.setRadius(0);
            date.setTextColor(ContextCompat.getColor(context, R.color.colorBackgroundVeryLight));
        }
    }

    @Override
    public boolean isHeader(int headerPosition) {
        return getItemViewType(headerPosition) == VIEW_TYPE_HEADER;
    }

    class ViewHolderItem extends RecyclerView.ViewHolder {

        AppCompatTextView name;
        AppCompatTextView classroom;
        AppCompatTextView time;
        View container;

        public ViewHolderItem(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.main_view_appointments_list_item_name);
            classroom = itemView.findViewById(R.id.main_view_appointments_list_item_classroom);
            time = itemView.findViewById(R.id.main_view_appointments_list_item_time);
            container = itemView.findViewById(R.id.main_view_appointments_list_item_container);
        }
    }

    class ViewHolderItemEmpty extends RecyclerView.ViewHolder {

        AppCompatTextView text;

        public ViewHolderItemEmpty(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.main_view_appointments_list_item_empty_text);
        }
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder {

        AppCompatTextView name;

        public ViewHolderHeader(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.main_view_appointments_list_header_name);
        }
    }

    public interface ItemOnSelectListener {
        void onSelect(AppointmentDto item);
    }

    public void setItemOnSelectListener(ItemOnSelectListener listener) {
        this.listener = listener;
    }
}
