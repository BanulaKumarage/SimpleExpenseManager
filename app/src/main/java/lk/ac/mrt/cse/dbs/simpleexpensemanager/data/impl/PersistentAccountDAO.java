package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO extends DataBaseHelper implements AccountDAO {
    private List<String> accountNos;
    private List<Account> accounts;

    public PersistentAccountDAO(Context context) {
        super(context);
        this.accountNos = new ArrayList<String>();
        this.accounts = new ArrayList<Account>();
    }

    @Override
    public List<String> getAccountNumbersList() {
        this.accountNos = new ArrayList<String>();

        String sql = "select accountNo from accounts";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()){

            do{
               this.accountNos.add(cursor.getString(0));
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return this.accountNos;
    }

    @Override
    public List<Account> getAccountsList() {
        this.accounts = new ArrayList<Account>();

        String sql = "select * from accounts";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()){

            do{
                String accountNo = cursor.getString(0);
                String bankName = cursor.getString(1);
                String accountHolderName = cursor.getString(2);
                double balance = cursor.getInt(3);

                Account acc = new Account(accountNo,bankName,accountHolderName,balance);
                this.accounts.add(acc);


            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return this.accounts;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from accounts where accountNo = '"+ accountNo + "' ;";
        Cursor cursor = db.rawQuery(sql,null);
        Account acc = null;

        String bankName = cursor.getString(1);
        String accountHolderName = cursor.getString(2);
        double balance = cursor.getInt(3);

        acc = new Account(accountNo,bankName,accountHolderName,balance);

        cursor.close();
        db.close();
        return acc;
    }

    @Override
    public void addAccount(Account account) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("accountNo", account.getAccountNo());
        cv.put("bankName", account.getBankName());
        cv.put("accountHolderName", account.getAccountHolderName());
        cv.put("balance", account.getBalance());

        db.insert("accounts", null, cv);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        String sql = "delete from accounts where accountNo = '"+ accountNo + "' ;";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectsql = "select balance from accounts where accountNo = '"+ accountNo +"' ;";
        Cursor cursor = db.rawQuery(selectsql,null);
        cursor.moveToFirst();
        double balance = cursor.getDouble(0);
        switch(expenseType){
            case EXPENSE:
                balance  -= amount;
                break;
            case INCOME:
                balance  += amount;
                break;
        }

        String updateSql = "update accounts set balance = ? where accountNo = ? ;";
        SQLiteStatement statement = db.compileStatement(updateSql);
        statement.bindDouble(1,balance);
        statement.bindString(2,accountNo);
        statement.executeUpdateDelete();
        statement.close();
        cursor.close();
        db.close();

    }
}
