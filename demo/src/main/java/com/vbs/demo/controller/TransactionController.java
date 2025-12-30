package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj){
        User user = userRepo.findById((obj.getId()))
                .orElseThrow(()->new RuntimeException("Wrong Id"));
        double newbalance= user.getBalance()+ obj.getAmount();
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newbalance);
        t.setDescription("Rs"+obj.getAmount()+"Deposit Successfull");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Deposit Successfull";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj){
        User user = userRepo.findById((obj.getId()))
                .orElseThrow(()->new RuntimeException("Wrong Id"));
        double newbalance= user.getBalance() - obj.getAmount();
        if (newbalance<0){
            return "insufficient Balance";
        }
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newbalance);
        t.setDescription("Rs"+obj.getAmount()+"Withdrawal Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Withdrawal Successful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj){
        User sender = userRepo.findById(obj.getId())
                .orElseThrow(()-> new RuntimeException("Not found"));
        User rec = userRepo.findByUsername(obj.getUsername());
        if(rec==null){
            return "Receiver Not Found";
        }
        if(sender.getId()==rec.getId()){
            return "Self Transaction Not allowed";
        }
        if(obj.getAmount()<0){
            return "Invalid Amount";
        }

        double sbalance = sender.getBalance() - obj.getAmount();
        if(sbalance<0){
            return "Insufficient Amount";
        }
        double rbalance = rec.getBalance() + obj.getAmount();

        sender.setBalance(sbalance);
        rec.setBalance(rbalance);
        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs"+obj.getAmount()+"Sent to user "+obj.getUsername());
        t1.setUserId(obj.getId());

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rbalance);
        t2.setDescription("Rs"+obj.getAmount()+"Received From user "+ sender.getUsername());
        t2.setUserId(rec.getId());

        transactionRepo.save(t1);
        transactionRepo.save(t2);
        return "Transfer Done Successfully";

    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook (@PathVariable int id){
        return transactionRepo.findAllByUserId(id);
    }


}