package br.com.ale.dto;

public record UpdateAccountRequest (long id, String accountNumber, String accountType, String status){

}
