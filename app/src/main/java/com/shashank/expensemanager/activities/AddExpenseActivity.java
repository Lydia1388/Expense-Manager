package com.shashank.expensemanager.activities;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.shashank.expensemanager.R;
import com.shashank.expensemanager.transactionDb.AppDatabase;
import com.shashank.expensemanager.transactionDb.AppExecutors;
import com.shashank.expensemanager.transactionDb.TransactionEntry;
import com.shashank.expensemanager.transactionDb.TransactionViewModel;
import com.shashank.expensemanager.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddExpenseActivity extends AppCompatActivity {

    TextInputEditText amountTextInputEditText;
    TextInputEditText descriptionTextInputEditText;
    TextInputLayout amountTextInputLayout;
    TextInputLayout descriptionTextInputLayout;
    TextView dateTextView;
    LinearLayout dateLinearLayout;
    Spinner categorySpinner;
    ArrayList<String> categories;
    Calendar myCalendar;
    String description;
    Date dateOfExpense;

    private DatePickerDialog datePickerDialog;
    private static AppDatabase appDatabase;
    private static final String LOG_TAG = AddExpenseActivity.class.getSimpleName();

    //These variables contain data which will be stored permanently on hitting save button
    int amount;
    String categoryOfExpense;       //This parameter is to decide category in a transaction
    String categoryOfTransaction;  //This parameter to decide whether it is income and expense

    //Variable to keep track from where it came to this activity
    String intentFrom;

    TransactionViewModel transactionViewModel;

    int transactionid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        amountTextInputEditText = findViewById(R.id.amountTextInputEditText);
        descriptionTextInputEditText = findViewById(R.id.descriptionTextInputEditText);
        amountTextInputLayout = findViewById(R.id.amountTextInputLayout);
        descriptionTextInputLayout = findViewById(R.id.descriptionTextInputLayout);
        dateTextView = findViewById(R.id.dateTextView);
        dateLinearLayout = findViewById(R.id.dateLinerLayout);
        categorySpinner = findViewById(R.id.categorySpinner);

        appDatabase = AppDatabase.getInstance(getApplicationContext());


        transactionViewModel = ViewModelProviders.of(this)
                .get(TransactionViewModel.class);

        categories = new ArrayList<>();

        myCalendar = Calendar.getInstance();
        setDateToTextView();

        //First task here is to determine from where this activity is launched from the 4 possibilities

        Intent intent = getIntent();

        intentFrom = intent.getStringExtra("from");

        if (intentFrom.equals(Constants.addIncomeString)) {
            categoryOfTransaction = Constants.incomeCategory;
            setTitle(getString(R.string.add_income));
            categories.add(getString(R.string.Income));
            categorySpinner.setClickable(false);
            categorySpinner.setEnabled(false);
            categorySpinner.setAdapter(new ArrayAdapter<>(AddExpenseActivity.this, android.R.layout.simple_list_item_1, categories));

        } else if (intentFrom.equals(Constants.addExpenseString)) {
            categoryOfTransaction = Constants.expenseCategory;
            setTitle(getString(R.string.add_expense));

            categories.add(getString(R.string.Food));
            categories.add(getString(R.string.Travel));
            categories.add(getString(R.string.Clothes));
            categories.add(getString(R.string.Movies));
            categories.add(getString(R.string.Health));
            categories.add(getString(R.string.Snack));
            categories.add(getString(R.string.Transportation));
            categorySpinner.setAdapter(new ArrayAdapter<>(AddExpenseActivity.this,
                    android.R.layout.simple_list_item_1, categories));

        } else if (intentFrom.equals(Constants.editIncomeString)) {
            setTitle(getString(R.string.EditIncome));

            amountTextInputEditText.setText(String.valueOf(intent.getIntExtra("amount", 0)));
            amountTextInputEditText.setSelection(amountTextInputEditText.getText().length());
            descriptionTextInputEditText.setText(intent.getStringExtra("description"));
            descriptionTextInputEditText.setSelection(descriptionTextInputEditText.getText().length());
            transactionid=intent.getIntExtra("id",-1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(intent.getStringExtra("date"));
                myCalendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateTextView.setText(intent.getStringExtra("date"));

            categoryOfTransaction = Constants.incomeCategory;
            categories.add(getString(R.string.Income));
            categorySpinner.setClickable(false);
            categorySpinner.setEnabled(false);
            categorySpinner.setAdapter(new ArrayAdapter<>(AddExpenseActivity.this, android.R.layout.simple_list_item_1, categories));

        } else if (intentFrom.equals(Constants.editExpenseString)) {
            categoryOfTransaction = Constants.expenseCategory;
            setTitle(getString(R.string.EditExpense));
            amountTextInputEditText.setText(String.valueOf(intent.getIntExtra("amount", 0)));
            amountTextInputEditText.setSelection(amountTextInputEditText.getText().length());
            descriptionTextInputEditText.setText(intent.getStringExtra("description"));
            descriptionTextInputEditText.setSelection(descriptionTextInputEditText.getText().length());
            dateTextView.setText(intent.getStringExtra("date"));
            transactionid=intent.getIntExtra("id",-1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(intent.getStringExtra("date"));
                myCalendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            categories.add(getString(R.string.Food));
            categories.add(getString(R.string.Travel));
            categories.add(getString(R.string.Clothes));
            categories.add(getString(R.string.Movies));
            categories.add(getString(R.string.Health));
            categories.add(getString(R.string.Snack));
            categories.add(getString(R.string.Transportation));
            categorySpinner.setAdapter(new ArrayAdapter<>(AddExpenseActivity.this, android.R.layout.simple_list_item_1, categories));
            categorySpinner.setSelection(categories.indexOf(intent.getStringExtra("category")));
        }
        dateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    public void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDateToTextView();
            }
        };

        DatePickerDialog datePickerDialog=new DatePickerDialog(AddExpenseActivity.this,dateSetListener,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void setDateToTextView() {
        Date date = myCalendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateToBeSet = sdf.format(date);
        dateTextView.setText(dateToBeSet);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_expense_activty_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.saveButton:

                if (amountTextInputEditText.getText().toString().isEmpty()
                        || descriptionTextInputEditText.getText().toString().isEmpty()) {

                    if (amountTextInputEditText.getText().toString().isEmpty())
                        amountTextInputEditText.setError(getString(R.string.AmountError));
                    if (descriptionTextInputEditText.getText().toString().isEmpty())
                        descriptionTextInputEditText.setError(getString(R.string.DesError));

                } else {
                    amount = Integer.parseInt(amountTextInputEditText.getText().toString());
                    description = descriptionTextInputEditText.getText().toString();
                    dateOfExpense = myCalendar.getTime();

                    if (intentFrom.equals(Constants.addIncomeString)
                            || intentFrom.equals(Constants.editIncomeString))
                        categoryOfExpense = getString(R.string.Income);
                    else
                        categoryOfExpense = categories.get(categorySpinner.getSelectedItemPosition());

                    final TransactionEntry mTransactionEntry = new TransactionEntry(amount,
                            categoryOfExpense,
                            description,
                            dateOfExpense,
                            categoryOfTransaction
                    );

                    if(intentFrom.equals(Constants.addIncomeString)||intentFrom.equals(Constants.addExpenseString)) {


                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                appDatabase.transactionDao().insertExpense(mTransactionEntry);
                            }
                        });

                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        Snackbar.make(getCurrentFocus(),getString(R.string.Added),Snackbar.LENGTH_LONG).show();
                    }
                    else{
                        mTransactionEntry.setId(transactionid);
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                appDatabase.transactionDao().updateExpenseDetails(mTransactionEntry);
                            }
                        });

                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        Snackbar.make(getCurrentFocus(),getString(R.string.Updated),Snackbar.LENGTH_LONG).show();

                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
                break;
        }
        return true;

    }

}

