/*
  Name: date_conversion.js
  Creation Date: 28/02/2014
  Author: Kwinten Pardon

  conversion from week day standard used by the VUB to the
*/

var start_date = new Date(2013,8,16,7,0,0,0);

/*

 */
function VUB_time_to_date(week, day, timeslot){
    var to_add = 1000 * 60 * ((week-1)*7*24*60
                              + (day-1)*24*60
                              + (timeslot-1)*30);
    return new Date(start_date.getTime() + to_add);
}

/*
 */
function date_to_VUB_time(date){
    var ms_from_start = date.getTime() - start_date.getTime();
    var m_from_start = ms_from_start/1000/60;
    var week = Math.floor(m_from_start/(7*24*60));
    var day = Math.floor((m_from_start - (week*7*24*60))
                         / (24*60));
    var timeslot = Math.floor((m_from_start - (week*7*24*60 + day*24*60))
                              / 30);
    return [week+1, day+1, timeslot+1];
}
