package org.kds.reactive.mapper;

import org.kds.reactive.model.Error;
import org.kds.reactive.model.FormatNameRequest;
import org.kds.reactive.model.FormattedNameResponse;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.List;
import java.util.stream.Collectors;

public class FormattedNameResponseMapper {

    private FormattedNameResponseMapper(){

    }

    public static FormattedNameResponse fromFormatNameRequest(FormatNameRequest req) {
        String s = req.getTitle() + " " + req.getFirstName() + " " + req.getMiddleName() + " " + req.getLastName();
        FormattedNameResponse res = new FormattedNameResponse();
        res.setFormattedName(s);
        return res;
    }

    public static FormattedNameResponse fromWebExchangeBindException(WebExchangeBindException ex) {
        FormattedNameResponse res = new FormattedNameResponse();
        List<Error> errors = ex.getFieldErrors().stream()
                .map(fieldError -> {
                    String code = fieldError.getField();
                    String message = fieldError.getDefaultMessage();
                    return new Error(code, message);
                }).collect(Collectors.toList());
        res.setErrors(errors);
        return res;
    }
}
