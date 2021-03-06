package com.example.inventorycontrolsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adedom.library.Dru;
import com.adedom.library.ExecuteQuery;
import com.adedom.library.ExecuteUpdate;
import com.example.inventorycontrolsystem.models.Product;
import com.example.inventorycontrolsystem.models.Type;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class EditProductActivity extends AppCompatActivity {

    private EditText mEtProductId;
    private EditText mEtProductName;
    private EditText mEtPrice;
    private EditText mEtQty;
    private ImageView mIvImage;
    private Button mBtCancle;
    private Button mBtOk;
    private Bitmap mBitmap;
    private ImageView mIvAddType;
    private Spinner mESpinner;
    private ArrayList<Type> items;
    private String mTypeId;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

//        String product_id=getIntent().getStringExtra("product_id");
//        String product_name=getIntent().getStringExtra("product_name");
//        double price=getIntent().getDoubleExtra("price",0.0);
//        int qty=getIntent().getIntExtra("qty",0);
//        String image=getIntent().getStringExtra("image");
//        String typeId=getIntent().getStringExtra("type_id");
//        String type_name=getIntent().getStringExtra("type_name");

         product=getIntent().getParcelableExtra("product");

        mEtProductId = (EditText) findViewById(R.id.et_product_id);
        mEtProductName = (EditText) findViewById(R.id.et_product_name);
        mEtPrice = (EditText) findViewById(R.id.et_price);
        mEtQty = (EditText) findViewById(R.id.et_qty);
        mESpinner = (Spinner) findViewById(R.id.spinner);
        mIvAddType = (ImageView) findViewById(R.id.iv_add_type);
        mIvImage = (ImageView) findViewById(R.id.iv_image);
        mBtCancle = (Button) findViewById(R.id.bt_cancel);
        mBtOk = (Button) findViewById(R.id.bt_ok);


        mEtProductId.setText(product.getProductId());
        mEtProductName.setText(product.getProductName());
        mEtPrice.setText(product.getPrice()+"");
        mEtQty.setText(product.getQty()+"");
        Dru.loadImageCircle(mIvImage,ConnectDB.BASE_IMAGE+product.getImage());

        mIvAddType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), AddTypeActivity_.class));
            }
        });

        mBtCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mBtOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditProduct();
            }
        });

        mIvImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dru.selectImage(EditProductActivity.this, 1234);
            }
        });


//        mEtProductId.setText(product_id);
//        mEtProductName.setText(product_name);
//        mEtPrice.setText(price+"");
//        mEtQty.setText(qty+"");
//        Dru.loadImageCircle(mIvImage,ConnectDB.BASE_IMAGE+image);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpinner();
    }

    private void setSpinner() {
        String sql = "SELECT * FROM type ORDER BY type_id DESC";
        Dru.connection(ConnectDB.getconnection())
                .execute(sql)
                .commit(new ExecuteQuery() {
                    @Override
                    public void onComplete(ResultSet resultSet) {
                        try {
                            items = new ArrayList<Type>();
                            while (resultSet.next()) {
                                Type type = new Type();
                                type.setTypeId(resultSet.getString("type_id"));
                                type.setTypeName(resultSet.getString("type_name"));
                                items.add(type);

                            }
                            mESpinner.setAdapter(new TypeAdapter(getBaseContext(), items));


                            for(int i=0;i<items.size();i++){
                                mESpinner.setSelection(i);
                                mTypeId=items.get(i).getTypeId();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });

        mESpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                Type type = (Type) parent.getItemAtPosition(i);
                mTypeId = type.getTypeId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mIvImage.setImageBitmap(mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void EditProduct() {
        String productId = mEtProductId.getText().toString().trim();
        String productName = mEtProductName.getText().toString().trim();
        String price = mEtPrice.getText().toString().trim();
        String qty = mEtQty.getText().toString().trim();

        if (productName.isEmpty()) return;
        else if (price.isEmpty()) return;
        else if (qty.isEmpty()) return;

        String name = "empty";
        if (mBitmap != null) {
            name = UUID.randomUUID().toString().replace("-", "") + ".png";
            Dru.uploadImage(ConnectDB.BASE_IMAGE, name, mBitmap);
        }

        String sql = "UPDATE product SET product_name='"+productName+"',price='"+price+"',qty='"+qty+"',image='"+name+"',type_id='"+mTypeId+"' WHERE product_id='"+productId+"'";
        Dru.connection(ConnectDB.getconnection())
                .execute(sql)
                .commit(new ExecuteUpdate() {
                    @Override
                    public void onComplete() {
                        Toast.makeText(getBaseContext(), "Insert success", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private class TypeAdapter extends ArrayAdapter<Type> {
        public TypeAdapter(Context context, ArrayList<Type> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        private View initView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView tvTypeId = (TextView) convertView.findViewById(android.R.id.text1);
            TextView tvTypeName = (TextView) convertView.findViewById(android.R.id.text2);

            Type type = getItem(position);
            tvTypeId.setText((type.getTypeId()));
            tvTypeName.setText(type.getTypeName());

            return convertView;
        }

    }
}
