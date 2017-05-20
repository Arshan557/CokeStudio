package com.arshan.cokestudio;

/**
 * Created by Arshan on 19-Mar-2017.
 */
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

    private List<SongsPojo> songsPojoListsList;
    private SongClickListener songClickListener;
    public List<SongsPojo> orig;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView songTitle, artists;
        public ImageView cover;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            songTitle = (TextView) view.findViewById(R.id.song_title);
            artists = (TextView) view.findViewById(R.id.artists);
            cover = (ImageView) view.findViewById(R.id.cover_image);

            songTitle.setOnClickListener(this);
            artists.setOnClickListener(this);
            cover.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context,Player.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("DATA", songsPojoListsList.get(getPosition()));
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public SongsAdapter(Context context, List<SongsPojo> songsPojoListsList) {
        this.context=context;
        this.songsPojoListsList = songsPojoListsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_row_songs, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SongsPojo songsPojo = songsPojoListsList.get(position);
        holder.songTitle.setText(songsPojo.getSong());
        holder.artists.setText(songsPojo.getArtists());
        //holder.cover.setImageBitmap(songsPojo.getCover_image());
        Glide.with(context).load(songsPojo.getCover_image()).into(holder.cover);

    }

    /**
     * This method used to filter serach items
     * @return filter
     */
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<SongsPojo> results = new ArrayList<SongsPojo>();
                if (orig == null)
                    orig = songsPojoListsList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final SongsPojo g : orig) {
                            if (g.getSong().toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                songsPojoListsList = (ArrayList<SongsPojo>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setClickListener(SongClickListener songClickListener) {
        this.songClickListener = songClickListener;
    }

    @Override
    public int getItemCount() {
        return songsPojoListsList.size();
    }

    public interface SongClickListener {
        public void itemClicked(View view, int position);
    }
}