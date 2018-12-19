package com.example.abzal.todozilla.fragment;


import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abzal.todozilla.R;
import com.example.abzal.todozilla.activity.CategoryEditorActivity;
import com.example.abzal.todozilla.cache.SharedPrefsUtils;
import com.example.abzal.todozilla.client.TodozillaRestClient;
import com.example.abzal.todozilla.constant.ErrorProcessor;
import com.example.abzal.todozilla.constant.Mode;
import com.example.abzal.todozilla.constant.TodozillaApi;
import com.example.abzal.todozilla.model.Category;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CategoriesFragment extends Fragment {

    private CategoryAdapter categoryAdapter;
    private RecyclerView categoryRecyclerView;
    private FloatingActionButton createCategoryBtn;
    private LinearLayout emptyListView;
    private LinearLayout loadingView;

    public CategoriesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        initViews(view);
        setValues();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllCategories();
    }

    private void setValues() {
        getActivity().setTitle("Categories");
    }

    private void initViews(View view) {
        createCategoryBtn = view.findViewById(R.id.create_category_floating_button);
        createCategoryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CategoryEditorActivity.class);
            intent.putExtra("mode", Mode.CREATE.toString());
            getContext().startActivity(intent);
        });

        categoryRecyclerView = view.findViewById(R.id.category_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryAdapter = new CategoryAdapter();
        categoryRecyclerView.setAdapter(categoryAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(categoryRecyclerView.getContext(),
                layoutManager.getOrientation());
        categoryRecyclerView.addItemDecoration(dividerItemDecoration);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    List<Category> categories = categoryAdapter.getItems();
                    Category category = categories.get(viewHolder.getAdapterPosition());
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(getActivity());
                    }
                    builder.setTitle("Delete category")
                            .setMessage("All tasks related to this category will also be deleted!")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                String token = SharedPrefsUtils.getStringPreference(getActivity(), "token");

                                TodozillaRestClient.delete(getContext(),
                                        TodozillaApi.CATEGORIES_ROUTE + "/" + category.getId(),
                                        new Header[]{TodozillaRestClient.createAuthTokenHeader(token)},
                                        null,
                                        new AsyncHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                Toast.makeText(getActivity(), "Category was deleted!", Toast.LENGTH_SHORT).show();
                                                getAllCategories();
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                Toast.makeText(getActivity(), "Delete Category. Error Code:" + statusCode, Toast.LENGTH_SHORT).show();
                                                categoryAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                            }
                                        });
                            })
                            .setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
                                categoryAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            }))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(categoryRecyclerView);

        emptyListView = view.findViewById(R.id.category_list_empty);
        loadingView = view.findViewById(R.id.loading_screen);
    }

    private void getAllCategories() {
        enableLoadingView();
        String token = SharedPrefsUtils.getStringPreference(getActivity(), "token");
        Log.d(CategoriesFragment.class.getName(), token);
        TodozillaRestClient.get(getActivity(), TodozillaApi.CATEGORIES_ROUTE, new Header[]{TodozillaRestClient.createAuthTokenHeader(token)}, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (response.length() == 0) {
                    enableEmptyListView();
                } else {
                    List<Category> categories = Category.parseJsonArray(response);
                    if (!categories.isEmpty()) {
                        enableCategoryListView();
                        categoryAdapter.setItems(categories);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                enableEmptyListView();
                ErrorProcessor.handle(getView(), getContext(), statusCode);
            }
        });
    }

    private void enableEmptyListView() {
        emptyListView.setVisibility(View.VISIBLE);
        categoryRecyclerView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
    }


    private void enableCategoryListView() {
        emptyListView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        categoryRecyclerView.setVisibility(View.VISIBLE);
    }

    private void enableLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        emptyListView.setVisibility(View.GONE);
        categoryRecyclerView.setVisibility(View.GONE);
    }


    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private List<Category> categoryList = new ArrayList<>();


        public void setItems(Collection<Category> categories) {
            categoryList.clear();
            categoryList.addAll(categories);
            notifyDataSetChanged();
        }

        public List<Category> getItems() {
            return categoryList;
        }

        public void clearItems() {
            categoryList.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_category, viewGroup, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder categoryViewHolder, int i) {
            Category category = categoryList.get(i);
            categoryViewHolder.title.setText(category.getTitle());
            categoryViewHolder.itemView.setOnClickListener(v -> {
                final Dialog dialog = new Dialog(getContext());
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_category);
                TextView title = dialog.findViewById(R.id.dialog_category_title);
                title.setText(category.getTitle());
                TextView description = dialog.findViewById(R.id.dialog_category_description);
                description.setText(category.getDescription());
                dialog.show();
            });

            categoryViewHolder.itemView.setOnLongClickListener(v -> {
                Intent intent = new Intent(getContext(), CategoryEditorActivity.class);
                intent.putExtra("id", category.getId());
                intent.putExtra("mode", Mode.UPDATE.toString());
                getContext().startActivity(intent);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return categoryList.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {

            private TextView title;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.category_item_title);
            }
        }

    }

}
