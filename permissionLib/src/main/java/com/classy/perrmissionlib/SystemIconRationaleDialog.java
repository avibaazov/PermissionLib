package com.classy.perrmissionlib;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class SystemIconRationaleDialog extends Dialog {

    private final List<String> deniedPermissions;
    private Runnable onPositiveClick;
    private Runnable onNegativeClick;
    private String explanationMessage; // Optional explanation text

    public SystemIconRationaleDialog(@NonNull Context context, String[] deniedPermissions) {
        // Apply the custom dialog theme overlay defined in your styles.xml
        super(context, R.style.Theme_PermissionLib_Dialog);
        this.deniedPermissions = Arrays.asList(deniedPermissions);
    }

    public void setExplanationMessage(String explanationMessage) {
        this.explanationMessage = explanationMessage;
    }

    public void setOnPositiveClick(Runnable onPositiveClick) {
        this.onPositiveClick = onPositiveClick;
    }

    public void setOnNegativeClick(Runnable onNegativeClick) {
        this.onNegativeClick = onNegativeClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the dialog layout from dialog_permission.xml
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_improved_custom, null);
        setContentView(view);

        // Optionally update the explanation text if provided
        if (explanationMessage != null && !explanationMessage.isEmpty()) {
            ((android.widget.TextView) view.findViewById(R.id.dialogExplanation)).setText(explanationMessage);
        }

        // Set up the RecyclerView for displaying denied permissions
        RecyclerView recyclerView = view.findViewById(R.id.permissionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PermissionListAdapter(getContext(), deniedPermissions));

        // Wire up the action buttons
        MaterialButton positiveButton = view.findViewById(R.id.positiveButton);
        MaterialButton negativeButton = view.findViewById(R.id.negativeButton);

        positiveButton.setOnClickListener(v -> {
            dismiss();
            if (onPositiveClick != null) {
                onPositiveClick.run();
            }
        });

        negativeButton.setOnClickListener(v -> {
            dismiss();
            if (onNegativeClick != null) {
                onNegativeClick.run();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Adjust dialog width to 85% of screen width
        if (getWindow() != null) {
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            int width = (int) (dm.widthPixels * 0.85);
            getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    // Inner adapter class for the RecyclerView
    private static class PermissionListAdapter extends RecyclerView.Adapter<PermissionListAdapter.ViewHolder> {

        private final Context context;
        private final List<String> permissions;

        public PermissionListAdapter(Context context, List<String> permissions) {
            this.context = context;
            this.permissions = permissions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_permission, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String permission = permissions.get(position);
            String displayName = permission.replace("android.permission.", "").replace("_", " ");
            // Try to get a more user-friendly label via PackageManager, if available
            try {
                PackageManager pm = context.getPackageManager();
                PermissionInfo pInfo = pm.getPermissionInfo(permission, 0);
                if (pInfo.group != null) {
                    PermissionGroupInfo pgInfo = pm.getPermissionGroupInfo(pInfo.group, 0);
                    if (pgInfo.labelRes != 0) {
                        displayName = context.getString(pgInfo.labelRes);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("PermissionAdapter", "Permission not found: " + permission);
            }
            holder.permissionText.setText(displayName);
        }

        @Override
        public int getItemCount() {
            return permissions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView permissionText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                permissionText = itemView.findViewById(R.id.permission_text);
            }
        }
    }
}
