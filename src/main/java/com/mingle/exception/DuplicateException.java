package com.mingle.exception;

import com.mingle.MingleGroupDto;
import com.mingle.MingleLeagueDto;
import com.mingle.MingleLocationDto;
import com.mingle.MingleUserDto;
import com.mingle.entity.MingleLocation;
import com.mingle.entity.MingleUser;
import io.grpc.Metadata;
import io.grpc.Status;

public  class DuplicateException extends MingleException {
    public DuplicateException(String title, String description, String debugDetail) {
        super(title, description, debugDetail);
    }

    public DuplicateException(String title, String description, MingleGroupDto mingleGroupDto) {
        super(title,description, title+". Duplicate group found check name="+mingleGroupDto.getGroupName()+", zip="+ mingleGroupDto.getZip());
    }

    public DuplicateException(String title, String description, MingleLeagueDto mingleLeagueDto) {
        super(title,description, title+". Duplicate group found check name="+mingleLeagueDto.getEventName()+" and group="+ mingleLeagueDto.getMingleGroupDto().getId());
    }

    public DuplicateException(String title, String description, MingleLocationDto mingleLocationDto) {
        super(title,description, title+". Duplicate location found check location name="+mingleLocationDto.getLocationName()+" and league="+ mingleLocationDto.getMingleLeagueDto().getId());
    }
    public DuplicateException(String title, String description, MingleUserDto mingleUserDto) {
        super(title,description, title+". Duplicate user found check email="+mingleUserDto.getEmail()+" or username="+ mingleUserDto.getUsername());
    }

    @Override
    protected Metadata getMetaData() {
        Metadata metadata= new Metadata();
        Metadata.Key<String> httpStatusKey = Metadata.Key.of("http-status", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(httpStatusKey, "405"); // bad request
        return metadata;
    }

    @Override
    protected Status getStatus() {
        return Status.ALREADY_EXISTS;
    }
}


