package com.shashank.expensemanager.fragments;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.shashank.expensemanager.R;
import com.shashank.expensemanager.activities.MainActivity;
import com.shashank.expensemanager.transactionDb.AppDatabase;
import com.shashank.expensemanager.transactionDb.AppExecutors;
import com.shashank.expensemanager.transactionDb.TransactionViewModel;
import com.shashank.expensemanager.utils.Constants;
import com.shashank.expensemanager.utils.ExpenseList;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static com.shashank.expensemanager.activities.MainActivity.fab;


public class BalanceFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private AppDatabase mAppDb;
    PieChart pieChart;
    Spinner spinner;
    private TextView balanceTv,incomeTv,expenseTv,coin1,coin2,coin3;
    private TextView dateTv;
    private int balanceAmount,incomeAmount,expenseAmount;
    private int foodExpense,travelExpense,clothesExpense,moviesExpense,heathExpense,SnackExpense,TransExpense;
    long firstDate;
    ArrayList<ExpenseList> expenseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_balance,container,false);

        pieChart= view.findViewById(R.id.balancePieChart);
        spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        mAppDb = AppDatabase.getInstance(getContext());
        balanceTv = view.findViewById(R.id.totalAmountTextView);
        expenseTv = view.findViewById(R.id.amountForExpenseTextView);
        incomeTv = view.findViewById(R.id.amountForIncomeTextView);
        coin1 = view.findViewById(R.id.coin1);
        coin2 = view.findViewById(R.id.coin2);
        coin3 = view.findViewById(R.id.coin3);
        dateTv = view.findViewById(R.id.dateTextView);
        expenseList=new ArrayList<>();
        getAllBalanceAmount();
        setupPieChart();
        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.date_array,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i("fragment", String.valueOf(isVisibleToUser));
        if (isVisibleToUser){
            setupSpinner();
            fab.setVisibility(View.GONE);
        } else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    private void setupPieChart() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if(spinner.getSelectedItemPosition()==0)
                    getAllPieValues();
                else if(spinner.getSelectedItemPosition()==1) {
                    try {
                        getWeekPieValues();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else if(spinner.getSelectedItemPosition()==2){
                    try {
                        getMonthPieValues();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                expenseList.clear();
             if(foodExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Food),foodExpense));
             if(travelExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Travel),travelExpense));
             if(clothesExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Clothes),clothesExpense));
             if(moviesExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Movies),moviesExpense));
             if(heathExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Health),heathExpense));
             if(SnackExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Snack),SnackExpense));
             if(TransExpense!=0)
                 expenseList.add(new ExpenseList(getString(R.string.Transportation),TransExpense));
            }
        });


        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {

                List<PieEntry> pieEntries = new ArrayList<>();
                for(int i = 0 ; i <expenseList.size(); i++){
                    pieEntries.add(new PieEntry(expenseList.get(i).getAmount(),expenseList.get(i).getCategory()));
                }
                pieChart.setVisibility(View.VISIBLE);
                PieDataSet dataSet = new PieDataSet(pieEntries,null);

                ArrayList<Integer> colors = new ArrayList<>();
                for (int c : ColorTemplate.JOYFUL_COLORS)
                    colors.add(c);
                for (int c : ColorTemplate.MATERIAL_COLORS)
                    colors.add(c);
                dataSet.setColors(colors);
                PieData pieData = new PieData(dataSet);

                pieData.setValueTextSize(16);
                pieData.setValueTextColor(Color.WHITE);
                pieData.setValueFormatter(new PercentFormatter());
                pieChart.setUsePercentValues(true);
                pieChart.setData(pieData);
                pieChart.animateY(1000);
                pieChart.invalidate();

                pieChart.getDescription().setText("");
                Legend l=pieChart.getLegend();
                l.setForm(Legend.LegendForm.SQUARE);
                l.setXEntrySpace(7f);
                l.setYEntrySpace(0f);
                l.setYOffset(8f);
                l.setFormSize(17);
                l.setTextSize(15f);
            }
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(adapterView.getSelectedItemPosition()==0){
            getAllBalanceAmount();
            setupPieChart();
        }

        else if (adapterView.getSelectedItemPosition() == 1){
            //This week
            try {
                getWeekBalanceAmount();
                setupPieChart();
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else if(adapterView.getSelectedItemPosition()==2){
            //This month
            try {
                getMonthBalanceAmount();
                setupPieChart();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }


    private void getAllPieValues(){
        foodExpense =mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Food));
        travelExpense=mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Travel));
        clothesExpense=mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Clothes));
        moviesExpense=mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Movies));
        heathExpense=mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Health));
        SnackExpense=mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Snack));
        TransExpense=mAppDb.transactionDao().getSumExpenseByCategory(getString(R.string.Transportation));
    }

    private void getWeekPieValues() throws ParseException {
        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String startDate = "", endDate = "";
        // Set the calendar to sunday of the current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.add(Calendar.DATE, 6);
        endDate = df.format(calendar.getTime());
        Date eDate=df.parse(endDate);
        final long edate=eDate.getTime();

        foodExpense =mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Food),sdate,edate);
        travelExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Travel),sdate,edate);
        clothesExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Clothes),sdate,edate);
        moviesExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Movies),sdate,edate);
        heathExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Health),sdate,edate);
        SnackExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Snack),sdate,edate);
        TransExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Transportation),sdate,edate);
    }

    private void getMonthPieValues() throws ParseException{

        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String startDate = "", endDate = "";

        calendar.set(Calendar.DAY_OF_MONTH,1);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = df.format(calendar.getTime());
        Date eDate=df.parse(endDate);
        final long edate=eDate.getTime();

        foodExpense =mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Food),sdate,edate);
        travelExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Travel),sdate,edate);
        clothesExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Clothes),sdate,edate);
        moviesExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Movies),sdate,edate);
        heathExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Health),sdate,edate);
        SnackExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Snack),sdate,edate);
        TransExpense=mAppDb.transactionDao().getSumExpenseByCategoryCustomDate(getString(R.string.Transportation),sdate,edate);
    }

    private void getAllBalanceAmount(){

        //get date when first transaction date and todays date
       AppExecutors.getInstance().diskIO().execute(new Runnable() {
           @Override
           public void run() {
               firstDate=mAppDb.transactionDao().getFirstDate();
           }
       });

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String first = df.format(new Date(firstDate));
        Date today=Calendar.getInstance().getTime();
        String todaysDate=df.format(today);
        String Date=first+" - "+todaysDate;
        dateTv.setText(Date);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                int income = mAppDb.transactionDao().getAmountByTransactionType(Constants.incomeCategory);
                incomeAmount = income;
                int expense = mAppDb.transactionDao().getAmountByTransactionType(Constants.expenseCategory);
                expenseAmount = expense;
                int balance = income - expense;
                balanceAmount = balance;
            }
        });
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                balanceTv.setText(String.valueOf(balanceAmount));
                incomeTv.setText(String.valueOf(incomeAmount));
                expenseTv.setText(String.valueOf(expenseAmount));
                coin1.setText(getString(R.string.currency));
                coin2.setText(getString(R.string.currency));
                coin3.setText(getString(R.string.currency));
            }
        });


    }

    private void getWeekBalanceAmount() throws ParseException {
        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String startDate = "", endDate = "";
        // Set the calendar to sunday of the current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.add(Calendar.DATE, 6);
        endDate = df.format(calendar.getTime());
        Date eDate=df.parse(endDate);
        final long edate=eDate.getTime();

        String dateString = startDate + " - " + endDate;
        dateTv.setText(dateString);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                int income = mAppDb.transactionDao().getAmountbyCustomDates(Constants.incomeCategory,sdate,edate);
                incomeAmount = income;
                int expense = mAppDb.transactionDao().getAmountbyCustomDates(Constants.expenseCategory,sdate,edate);
                expenseAmount = expense;
                int balance = income - expense;
                balanceAmount = balance;

            }
        });
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                balanceTv.setText(String.valueOf(balanceAmount));
                incomeTv.setText(String.valueOf(incomeAmount));
                expenseTv.setText(String.valueOf(expenseAmount));
                coin1.setText(getString(R.string.currency));
                coin2.setText(getString(R.string.currency));
                coin3.setText(getString(R.string.currency));
            }
        });
    }


    private void getMonthBalanceAmount() throws ParseException {
        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String startDate = "", endDate = "";

        calendar.set(Calendar.DAY_OF_MONTH,1);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = df.format(calendar.getTime());
        Date eDate=df.parse(endDate);
        final long edate=eDate.getTime();

        String dateString = startDate + " - " + endDate;
        dateTv.setText(dateString);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                int income = mAppDb.transactionDao().getAmountbyCustomDates(Constants.incomeCategory,sdate,edate);
                incomeAmount = income;
                int expense = mAppDb.transactionDao().getAmountbyCustomDates(Constants.expenseCategory,sdate,edate);
                expenseAmount = expense;
                int balance = income - expense;
                balanceAmount = balance;

            }
        });
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                balanceTv.setText(String.valueOf(balanceAmount));
                incomeTv.setText(String.valueOf(incomeAmount));
                expenseTv.setText(String.valueOf(expenseAmount));
                coin1.setText(getString(R.string.currency));
                coin2.setText(getString(R.string.currency));
                coin3.setText(getString(R.string.currency));
            }
        });
    }
}
