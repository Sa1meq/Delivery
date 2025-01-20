package com.example.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delivery.FaqItem;
import com.example.delivery.R;
import com.example.delivery.adapter.FaqAdapter;

import java.util.Arrays;
import java.util.List;

public class SupportActivity extends AppCompatActivity {

    private RecyclerView faqRecyclerView;
    private Button supportButton;
    private ImageView backImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        faqRecyclerView = findViewById(R.id.faqRecyclerView);
        supportButton = findViewById(R.id.supportButton);
        backImageView = findViewById(R.id.backImageView);

        List<FaqItem> faqList = Arrays.asList(
                new FaqItem("Почему не привязывается карта?", "Убедитесь, что вы ввели данные карты корректно."),
                new FaqItem("Почему я не вижу свой заказ?", "Проверьте подключение к интернету и обновите список заказов."),
                new FaqItem("Как связаться с поддержкой?", "Используйте кнопку внизу для связи с техподдержкой."),
                new FaqItem("Как выбрать тариф доставки?", "Выберите тариф в процессе оформления заказа в зависимости от времени и расстояния."),
                new FaqItem("Как изменить способ оплаты?", "Вы можете изменить способ оплаты в настройках вашего профиля."),
                new FaqItem("Как оценить курьера?", "После завершения доставки вы получите возможность оставить отзыв и оценку курьеру."),
                new FaqItem("Что делать, если я не доволен обслуживанием?", "Вы можете оставить жалобу через раздел 'Обратная связь' в приложении.")
        );

        FaqAdapter adapter = new FaqAdapter(faqList);
        faqRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        faqRecyclerView.setAdapter(adapter);


        backImageView.setOnClickListener(v -> {
            Intent intent = new Intent(SupportActivity.this, UserProfile.class);
            startActivity(intent);
            finish();
        });
   supportButton.setOnClickListener(v -> {
          Intent intent = new Intent(SupportActivity.this, SupportChatListActivity.class);
        startActivity(intent);
       });
    }

}
